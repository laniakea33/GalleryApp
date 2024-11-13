package com.dh.galleryapp.feature.list

import android.util.Log
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dh.galleryapp.core.imagecache.CacheResult
import com.dh.galleryapp.core.imagecache.ImageCache
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.ui.components.LoadingScreen
import com.dh.galleryapp.core.ui.components.rememberImageCache
import com.dh.galleryapp.core.ui.components.toPx
import kotlinx.coroutines.Job
import java.net.URLEncoder

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
    onItemClick: (url: String, thumbnailKey: String) -> Unit,
) {
    val images = viewModel.images.collectAsLazyPagingItems()

    val count by remember {
        derivedStateOf {
            images.itemCount
        }
    }

    LaunchedEffect(count) {
        if (images.itemCount > 0) {
            Log.d(
                "dhlog",
                "ListScreen images.size : ${count} from : ${images?.get(0)?.id} : $images"
            )
        } else {
            Log.d("dhlog", "ListScreen images.size : ${count} : $images")
        }
    }

    if (images.loadState.refresh == LoadState.Loading) {
        LoadingScreen()
    } else {
        ImageList(
            images = images,
            modifier = modifier,
            onItemClick = onItemClick,
        )
    }
}

@Composable
fun ImageList(
    images: LazyPagingItems<Image>,
    modifier: Modifier = Modifier,
    onItemClick: (url: String, thumbnailKey: String) -> Unit,
) {
    Box(modifier = modifier) {
        val configuration = LocalConfiguration.current
        val itemWidth = configuration.screenWidthDp.dp / 2

        val state = rememberLazyGridState()

        val firstVisibleItemIndex by remember {
            derivedStateOf {
                state.firstVisibleItemIndex
            }
        }

        LaunchedEffect(firstVisibleItemIndex) {
            Log.d("dhlog", "ImageList LaunchedEffect() : $firstVisibleItemIndex")

        }


        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            state = state,
        ) {
            items(
                images.itemCount,
            ) { index ->
                val size = itemWidth.toPx().toInt()

                ImageListItem(
                    image = images[index]!!,
                    modifier = Modifier
                        .height(itemWidth)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onItemClick(images[index]!!.downloadUrl, "${URLEncoder.encode(images[index]!!.downloadUrl)}_${size}_$size.jpg")
                            }
                        ),
                    size = size,
                )
            }
        }
    }
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
    imageCache: ImageCache = rememberImageCache(),
    size: Int,
) {
    val cachedImage by imageCache.observe(image.downloadUrl, size, size)
        .collectAsState(CacheResult.Waiting)

    DisposableEffect(cachedImage) {
        Log.d("dhlog", "ImageListItem DisposableEffect() : id${image.id}, start : ${cachedImage}")
        val prev: CacheResult = cachedImage

        var job: Job? = null

        if (cachedImage is CacheResult.Waiting) {
            Log.d("dhlog", "ImageListItem DisposableEffect() : id${image.id}, 다시 실행")
            job = imageCache.requestImageSampling(image.downloadUrl, size, size, id = image.id)
            Log.d(
                "dhlog",
                "ImageListItem DisposableEffect() : id${image.id}, 다시 실행 후 ${job.isCancelled}"
            )
        }

        //  첫 로딩 떄 Waiting -> onDispose(Waiting -> Loading) -> Loading : 취소 안해야 함
        //  첫 로딩 중 스크롤 아웃 onDispose(Loading, Loading) : 취소 해야 함
        //  다시 돌아왔을 때 Waiting -> onDispose(Loading) -> Loading : Waiting -> Loading : 취소 안해야 함
        //  로딩 중 실패 onDispose(Loading -> Failure) -> Failure : 취소 안 해야 함
        //  로딩 중 성공 onDispose(Loading -> Success) -> Success : 취소 안 해야 함

        onDispose {
            Log.d("dhlog", "ImageListItem DisposableEffect() : id${image.id}, onDispose : $cachedImage, prev ; $prev")

            //  Loading으로 바뀌었을 때와 원래 Loading일 때를 구분해서 취소해야 함.
            //  Loading으로 바뀌면 취소를 안해야 함.
6
            if (cachedImage is CacheResult.Loading && prev is CacheResult.Loading) {
                Log.d("dhlog", "ImageListItem DisposableEffect() : id${image.id}, cancel")
                job?.cancel()
            }
        }
    }

    ImageListItemContent(cachedImage, modifier)
}

@Composable
private fun ImageListItemContent(
    cachedImage: CacheResult,
    modifier: Modifier
) {
    when (cachedImage) {
        CacheResult.Loading, CacheResult.Waiting -> {
            LoadingScreen(
                modifier = modifier,
            )
        }

        is CacheResult.Success -> {
            Image(
                bitmap = (cachedImage as CacheResult.Success).data.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }

        is CacheResult.Failure -> {
            Text(
                text = (cachedImage as CacheResult.Failure).t.message ?: "오류 발생",
                style = MaterialTheme.typography
                    .headlineMedium,
                modifier = modifier
                    .padding(8.dp)
                    .background(color = Color.LightGray),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
fun ImageListItemPreview() {
    ImageListItem(
        image = dummyImages[2],
        modifier = Modifier,
        size = 360,
    )
}