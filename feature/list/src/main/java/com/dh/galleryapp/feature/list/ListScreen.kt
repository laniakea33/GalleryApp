package com.dh.galleryapp.feature.list

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dh.galleryapp.core.ui.components.LoadingScreen
import com.dh.galleryapp.core.ui.components.toPx
import com.dh.galleryapp.feature.model.ImageResult.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

private const val columnCount = 2

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
    onItemClick: (url: String, thumbnailKey: String) -> Unit,
) {
    val imageRequestList = viewModel.imageRequestList.collectAsLazyPagingItems()
    val imageResultList = viewModel.imageResultList

    val isLoading by remember {
        derivedStateOf {
            imageRequestList.loadState.refresh == LoadState.Loading
        }
    }

    if (isLoading) {
        LoadingScreen()

    } else {
        val configuration = LocalConfiguration.current
        val itemSize = configuration.screenWidthDp.dp / columnCount
        val width = itemSize.toPx().toInt()
        val height = itemSize.toPx().toInt()

        PreloadImageList(
            imageRequestList = imageRequestList,
            itemSize = itemSize,
            imageResultList = imageResultList,
            modifier = modifier,
            onItemClick = onItemClick,
            onObserve = { index, downloadUrl ->
                runBlocking {
                    viewModel.observe(index, downloadUrl, width, height)
                }
            },
            onRequest = { index, downloadUrl ->
                viewModel.requestImageSampling(
                    downloadUrl,
                    width, height,
                )
            },
            onCancel = { index, downloadUrl ->
                runBlocking {
                    viewModel.dispose(
                        index,
                        com.dh.galleryapp.core.key.KeyGenerator.key(downloadUrl, width, height)
                    )
                }
            }
        )
    }
}

@Composable
fun PreloadImageList(
    imageRequestList: LazyPagingItems<com.dh.galleryapp.feature.model.ImageRequest>,
    itemSize: Dp,
    imageResultList: List<com.dh.galleryapp.feature.model.ImageResult>,
    modifier: Modifier = Modifier,
    onObserve: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onRequest: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onCancel: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
) {
    Box(modifier = modifier) {
        val lazyGridState = rememberLazyGridState()

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp

        val rowCountForOnePage by remember {
            derivedStateOf {
                (screenHeight.value.toInt() / itemSize.value.toInt() + 1)
            }
        }

        val lastVisibleItemIndex by remember {
            derivedStateOf {
                lazyGridState.firstVisibleItemIndex + rowCountForOnePage * columnCount - 1
            }
        }

        val onePageItemCount by remember {
            derivedStateOf {
                rowCountForOnePage * columnCount
            }
        }

        val initialItemLoadCount by remember {
            derivedStateOf {
                onePageItemCount * 2
            }
        }

        val recompositionLoadIndex by remember {
            derivedStateOf {
                val start = lastVisibleItemIndex + onePageItemCount - 1
                buildList {
                    for (i in 0 until columnCount) {
                        add(start + i)
                    }
                }
            }
        }

        var firstRequested by rememberSaveable {
            mutableStateOf(false)
        }

        val itemCount by remember {
            derivedStateOf {
                imageRequestList.itemCount
            }
        }

        var preloaded by rememberSaveable {
            mutableIntStateOf(-1)
        }

        LaunchedEffect(recompositionLoadIndex, initialItemLoadCount, itemCount) {
            if (imageRequestList.itemCount == 0) return@LaunchedEffect
            if (!firstRequested) {
                for (index in onePageItemCount until initialItemLoadCount) {
                    if (index in (preloaded + 1)..<itemCount) {
                        imageRequestList[index]?.let {
                            onRequest(index, it.downloadUrl)
                            preloaded = index
                        }
                    }
                }
                firstRequested = true

            } else {
                recompositionLoadIndex.forEach { index ->
                    if (index in (preloaded + 1)..<itemCount) {
                        imageRequestList[index]?.let {
                            onRequest(index, it.downloadUrl)
                            preloaded = index
                        }
                    }
                }
            }
        }

        ImageList(
            imageRequestList = imageRequestList,
            itemSize = itemSize,
            imageResultList = imageResultList,
            lazyGridState = lazyGridState,
            modifier = Modifier.fillMaxSize(),
            onObserve = onObserve,
            onRequest = onRequest,
            onCancel = onCancel,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun ImageList(
    imageRequestList: LazyPagingItems<com.dh.galleryapp.feature.model.ImageRequest>,
    lazyGridState: LazyGridState,
    itemSize: Dp,
    imageResultList: List<com.dh.galleryapp.feature.model.ImageResult>,
    modifier: Modifier = Modifier,
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
    onObserve: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onRequest: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onCancel: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
) {
    val width = itemSize.toPx().toInt()
    val height = itemSize.toPx().toInt()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columnCount),
        state = lazyGridState,
    ) {
        items(
            imageRequestList.itemCount,
        ) { index ->
            val imageRequest = imageRequestList[index]!!
            val imageResult = imageResultList.getOrNull(index)
                ?: Unknown

            ImageListItem(
                index = index,
                imageRequest = imageRequest,
                imageResult = imageResult,
                modifier = Modifier
                    .height(itemSize),
                onObserve = onObserve,
                onRequest = {
                    onRequest(index, imageRequest.downloadUrl)
                },
                onCancel = { onCancel(index, imageRequest.downloadUrl) },
                onClick = {
                    onItemClick(
                        imageRequest.downloadUrl,
                        com.dh.galleryapp.core.key.KeyGenerator.key(
                            url = imageRequest.downloadUrl,
                            width = width,
                            height = height,
                        ),
                    )
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Waiting() {
    PreloadImageList(
        imageRequestList = MutableStateFlow(PagingData.from(dummyImageRequestList)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        imageResultList = dummyImageResultList,
        modifier = Modifier,
        onObserve = { index, image ->
            MutableStateFlow(Waiting)
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Success() {
    val context = LocalContext.current

    val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_dialog_map)

    PreloadImageList(
        imageRequestList = MutableStateFlow(PagingData.from(dummyImageRequestList)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        imageResultList = dummyImageResultList,
        modifier = Modifier,
        onObserve = { index, image ->
            MutableStateFlow(Success(bitmap!!))
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Failure() {
    PreloadImageList(
        imageRequestList = MutableStateFlow(PagingData.from(dummyImageRequestList)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        imageResultList = dummyImageResultList,
        modifier = Modifier,
        onObserve = { index, image ->
            MutableStateFlow(Failure(RuntimeException("심각한 오류 발생")))
        },
    )
}

private val dummyImageRequestList = buildList {
    for (i in 0 until 10) {
        com.dh.galleryapp.feature.model.ImageRequest(
            id = i.toString(),
            downloadUrl = "https://picsum.photos/id/$i/200/300",
        ).also {
            add(it)
        }
    }
}

private val dummyImageResultList = buildList {
    for (i in 0 until 10) {
        add(Waiting)
    }
}

@Composable
fun ImageListItem(
    index: Int,
    imageRequest: com.dh.galleryapp.feature.model.ImageRequest,
    imageResult: com.dh.galleryapp.feature.model.ImageResult,
    modifier: Modifier = Modifier,
    onObserve: (index: Int, downloadUrl: String) -> Unit = { _, _ -> },
    onRequest: () -> Unit = {},
    onCancel: () -> Unit = {},
    onClick: () -> Unit,
) {
    LaunchedEffect(imageResult) {
        if (imageResult is Waiting) {
            onRequest()
        }
    }

    DisposableEffect(LocalContext.current) {
        onObserve(index, imageRequest.downloadUrl)
        onDispose {
            onCancel()
        }
    }

    ImageListItemContent(
        imageResult = imageResult,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun ImageListItemContent(
    imageResult: com.dh.galleryapp.feature.model.ImageResult,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    when (imageResult) {
        Unknown, Loading, Waiting -> {
            LoadingScreen(
                modifier = modifier,
            )
        }

        is Success -> {
            Image(
                bitmap = (imageResult as Success).data.asImageBitmap(),
                contentDescription = null,
                modifier = modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
                contentScale = ContentScale.Crop
            )
        }

        is Failure -> {
            Text(
                text = (imageResult as Failure).t.message
                    ?: "오류 발생",
                style = MaterialTheme.typography
                    .headlineMedium,
                modifier = modifier
                    .padding(8.dp)
                    .background(color = Color.LightGray),
            )
        }
    }
}