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
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.ui.components.LoadingScreen
import com.dh.galleryapp.core.ui.components.toPx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URLEncoder

@Composable
fun ListScreen(
    viewModel: ListViewModel,
    onItemClick: (url: String, thumbnailKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val images = viewModel.images.collectAsLazyPagingItems()

    if (images.loadState.refresh == LoadState.Loading) {
        LoadingScreen()

    } else {
        val configuration = LocalConfiguration.current
        val itemWidth = configuration.screenWidthDp.dp / 2

        val widthDp = itemWidth
        val heightDp = itemWidth
        val width = widthDp.toPx().toInt()
        val height = heightDp.toPx().toInt()

        ImageList(
            images = images,
            widthDp = widthDp,
            heightDp = heightDp,
            onItemClick = onItemClick,
            modifier = modifier,
            onRequest = { index ->
                val image = images[index]!!
                viewModel.requestImageSampling(
                    image.downloadUrl,
                    width, height,
                    id = image.id,
                )
            },
            onCancel = { index ->
                val image = images[index]!!
                viewModel.cancelJob(image.id)
            },
            onObserve = { index ->
                val image = images[index]!!
                viewModel.observe(image.downloadUrl, width, height)
            }
        )
    }
}

@Composable
fun ImageList(
    images: LazyPagingItems<Image>,
    widthDp: Dp,
    heightDp: Dp,
    modifier: Modifier = Modifier,
    onObserve: (index: Int) -> StateFlow<CacheState> = { _ -> MutableStateFlow(CacheState.Waiting) },
    onItemClick: (url: String, thumbnailKey: String) -> Unit = { _, _ -> },
    onRequest: (index: Int) -> Unit = {},
    onCancel: (index: Int) -> Unit = {},
) {
    val width = widthDp.toPx().toInt()
    val height = heightDp.toPx().toInt()

    Box(modifier = modifier) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
        ) {
            items(
                images.itemCount,
            ) { index ->
                val image = images[index]!!

                val cachedImage by onObserve(index)
                    .collectAsState(CacheState.Waiting)

                ImageListItem(
                    image = image,
                    modifier = Modifier
                        .height(heightDp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onItemClick(
                                    images[index]!!.downloadUrl,
                                    "${URLEncoder.encode(image.downloadUrl)}_${width}_$height.jpg"
                                )
                            }
                        ),
                    cachedImage = cachedImage,
                    onRequest = {
                        onRequest(index)
                    },
                    onCancel = { onCancel(index) }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Waiting() {
    ImageList(
        modifier = Modifier,
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        widthDp = 180.dp,
        heightDp = 180.dp,
        onObserve = { index ->
            MutableStateFlow(CacheState.Waiting)
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Success() {
    val context = LocalContext.current

    val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_dialog_map)

    ImageList(
        modifier = Modifier,
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        widthDp = 180.dp,
        heightDp = 180.dp,
        onObserve = { index ->
            MutableStateFlow(CacheState.Success(bitmap!!))
        },
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 640)
fun ImageListPreview_Failure() {
    ImageList(
        modifier = Modifier,
        images = MutableStateFlow(PagingData.from(dummyImages)).collectAsLazyPagingItems(),
        widthDp = 180.dp,
        heightDp = 180.dp,
        onObserve = { index ->
            MutableStateFlow(CacheState.Failure(RuntimeException("심각한 오류 발생")))
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
    image: Image,
    modifier: Modifier = Modifier,
    cachedImage: CacheState,
    onRequest: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    DisposableEffect(cachedImage) {
        val prev: CacheState = cachedImage

        if (cachedImage is CacheState.Waiting) {
            onRequest()
        }

        onDispose {
            if (cachedImage is CacheState.Loading && prev is CacheState.Loading) {
                onCancel()
            }
        }
    }

    ImageListItemContent(cachedImage, modifier)
}

@Composable
private fun ImageListItemContent(
    cachedImage: CacheState,
    modifier: Modifier
) {
    when (cachedImage) {
        CacheState.Loading, CacheState.Waiting -> {
            LoadingScreen(
                modifier = modifier,
            )
        }

        is CacheState.Success -> {
            Image(
                bitmap = (cachedImage as CacheState.Success).data.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }

        is CacheState.Failure -> {
            Text(
                text = (cachedImage as CacheState.Failure).t.message ?: "오류 발생",
                style = MaterialTheme.typography
                    .headlineMedium,
                modifier = modifier
                    .padding(8.dp)
                    .background(color = Color.LightGray),
            )
        }
    }
}