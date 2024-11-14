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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dh.galleryapp.core.KeyGenerator
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.ui.components.LoadingScreen
import com.dh.galleryapp.core.ui.components.toPx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ListScreen(
    viewModel: ListViewModel,
    modifier: Modifier = Modifier,
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
        val itemSize = configuration.screenWidthDp.dp / 2
        val width = itemSize.toPx().toInt()
        val height = itemSize.toPx().toInt()

        ImageList(
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
                viewModel.cancelJob(KeyGenerator.key(it.downloadUrl, width, height))
            },
            onObserve = {
                viewModel.observe(it.downloadUrl, width, height)
            }
        )
    }
}

@Composable
fun ImageList(
    images: LazyPagingItems<Image>,
    itemSize: Dp,
    modifier: Modifier = Modifier,
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
    onObserve: (image: Image) -> StateFlow<ImageState> = { _ -> MutableStateFlow(ImageState.Waiting) },
    onRequest: (image: Image) -> Unit = {},
    onCancel: (image: Image) -> Unit = {},
) {
    Box(modifier = modifier) {
        val width = itemSize.toPx().toInt()
        val height = itemSize.toPx().toInt()

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
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
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Waiting() {
    ImageList(
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

    ImageList(
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
    ImageList(
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
    DisposableEffect(cachedImage) {
        val prev: ImageState = cachedImage

        if (cachedImage is ImageState.Waiting) {
            onRequest()
        }

        onDispose {
//            if (cachedImage is ImageState.Loading && prev is ImageState.Loading) {
//                onCancel()
//            }
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