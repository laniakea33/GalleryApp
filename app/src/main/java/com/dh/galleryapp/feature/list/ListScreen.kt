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
import androidx.compose.runtime.collectAsState
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
import com.dh.galleryapp.core.key.KeyGenerator
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.ui.components.LoadingScreen
import com.dh.galleryapp.core.ui.components.toPx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

private const val columnCount = 2

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
    onItemClick: (url: String, thumbnailKey: String) -> Unit,
) {
    val images = viewModel.images.collectAsLazyPagingItems()

    val isLoading by remember {
        derivedStateOf {
            images.loadState.refresh == LoadState.Loading
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
            images = images,
            itemSize = itemSize,
            modifier = modifier,
            onItemClick = onItemClick,
            onRequest = {
                viewModel.requestImageSampling(
                    it.downloadUrl,
                    width, height,
                    id = it.id,
                )
            },
            onCancel = {
                runBlocking {
                    viewModel.cancelRequest(KeyGenerator.key(it.downloadUrl, width, height))
                }
            },
            onObserve = {
                runBlocking {
                    viewModel.observe(it.downloadUrl, width, height)
                }
            }
        )
    }
}

@Composable
fun PreloadImageList(
    images: LazyPagingItems<Image>,
    itemSize: Dp,
    modifier: Modifier = Modifier,
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
    onObserve: (image: Image) -> StateFlow<ImageState> = { _ -> MutableStateFlow(ImageState.Waiting) },
    onRequest: (image: Image) -> Unit = {},
    onCancel: (image: Image) -> Unit = {},
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
                images.itemCount
            }
        }

        var preloaded by rememberSaveable {
            mutableIntStateOf(-1)
        }

        LaunchedEffect(recompositionLoadIndex, initialItemLoadCount, itemCount) {
            if (images.itemCount == 0) return@LaunchedEffect
            if (!firstRequested) {
                for (index in onePageItemCount until initialItemLoadCount) {
                    if (index in (preloaded + 1)..<itemCount) {
                        images[index]?.let {
                            onRequest(it)
                            preloaded = index
                        }
                    }
                }
                firstRequested = true

            } else {
                recompositionLoadIndex.forEach { index ->
                    if (index in (preloaded + 1)..<itemCount) {
                        images[index]?.let {
                            onRequest(it)
                            preloaded = index
                        }
                    }
                }
            }
        }

        ImageList(
            modifier = Modifier.fillMaxSize(),
            lazyGridState = lazyGridState,
            images = images,
            onObserve = onObserve,
            itemSize = itemSize,
            onRequest = onRequest,
            onCancel = onCancel,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun ImageList(
    images: LazyPagingItems<Image>,
    lazyGridState: LazyGridState,
    itemSize: Dp,
    modifier: Modifier = Modifier,
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
    onObserve: (image: Image) -> StateFlow<ImageState> = { _ -> MutableStateFlow(ImageState.Waiting) },
    onRequest: (image: Image) -> Unit = {},
    onCancel: (image: Image) -> Unit = {},
) {
    val width = itemSize.toPx().toInt()
    val height = itemSize.toPx().toInt()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columnCount),
        state = lazyGridState,
    ) {
        items(
            images.itemCount,
        ) { index ->
            val image = images[index]!!

            val cachedImage by onObserve(image)
                .collectAsState(ImageState.Waiting)

            ImageListItem(
                cachedImage = cachedImage,
                modifier = Modifier
                    .height(itemSize),
                onRequest = { onRequest(image) },
                onCancel = { onCancel(image) },
                onClick = {
                    onItemClick(
                        image.downloadUrl,
                        KeyGenerator.key(
                            url = image.downloadUrl,
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
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        modifier = Modifier,
        onObserve = { index ->
            MutableStateFlow(ImageState.Waiting)
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Success() {
    val context = LocalContext.current

    val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_dialog_map)

    PreloadImageList(
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        modifier = Modifier,
        onObserve = { index ->
            MutableStateFlow(ImageState.Success(bitmap!!))
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Failure() {
    PreloadImageList(
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        itemSize = 180.dp,
        modifier = Modifier,
        onObserve = { index ->
            MutableStateFlow(ImageState.Failure(RuntimeException("심각한 오류 발생")))
        },
    )
}

private val dummyImages = buildList {
    for (i in 0 until 10) {
        Image(
            id = i.toString(),
            author = "Alejandro Escamilla",
            width = 5000,
            height = 3000,
            url = "https://unsplash.com/photos/yC-Yzbqy7PY",
            downloadUrl = "https://picsum.photos/id/$i/200/300",
        ).also {
            add(it)
        }
    }
}

@Composable
fun ImageListItem(
    cachedImage: ImageState,
    modifier: Modifier = Modifier,
    onRequest: () -> Unit = {},
    onCancel: () -> Unit = {},
    onClick: () -> Unit,
) {
    LaunchedEffect(cachedImage) {
        if (cachedImage is ImageState.Waiting) {
            onRequest()
        }
    }

    DisposableEffect(LocalContext.current) {
        onDispose {
            onCancel()
        }
    }

    ImageListItemContent(
        cachedImage = cachedImage,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun ImageListItemContent(
    cachedImage: ImageState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    when (cachedImage) {
        ImageState.Loading, ImageState.Waiting -> {
            LoadingScreen(
                modifier = modifier,
            )
        }

        is ImageState.Success -> {
            Image(
                bitmap = (cachedImage as ImageState.Success).data.asImageBitmap(),
                contentDescription = null,
                modifier = modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
                contentScale = ContentScale.Crop
            )
        }

        is ImageState.Failure -> {
            Text(
                text = (cachedImage as ImageState.Failure).t.message ?: "오류 발생",
                style = MaterialTheme.typography
                    .headlineMedium,
                modifier = modifier
                    .padding(8.dp)
                    .background(color = Color.LightGray),
            )
        }
    }
}