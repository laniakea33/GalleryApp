package com.dh.galleryapp.feature.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dh.galleryapp.core.ui.components.ZoomableImage
import com.dh.galleryapp.feature.list.ImageState
import com.dh.galleryapp.feature.list.ListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    url: String,
    thumbnailKey: String,
    viewModel: ListViewModel = hiltViewModel(),
) {
    Box(
        modifier = modifier
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        DetailImage(
            url = url,
            thumbnail = viewModel.requestImageWithKey(thumbnailKey).asImageBitmap(),
            modifier = modifier,
            onObserve = {
                runBlocking {
                    viewModel.observe(url)
                }
            },
            onRequest = {
                viewModel.requestImage(url)
            },
            onCancel = {
                runBlocking {
                    viewModel.cancelRequest(url)
                }
            }
        )
    }
}

@Composable
private fun DetailImage(
    url: String,
    thumbnail: ImageBitmap,
    modifier: Modifier = Modifier,
    onObserve: (url: String) -> StateFlow<ImageState> = { _ -> MutableStateFlow(ImageState.Waiting) },
    onRequest: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val cachedImage by onObserve(url)
        .collectAsState(ImageState.Waiting)

    val cacheState by remember {
        derivedStateOf {
            cachedImage
        }
    }

    DisposableEffect(cacheState) {
        val prev: ImageState = cacheState

        if (cachedImage is ImageState.Waiting) {
            onRequest()
        }

        onDispose {
            if (cacheState is ImageState.Loading && prev is ImageState.Loading) {
                onCancel()
            }
        }
    }

    when (cacheState) {
        ImageState.Loading, ImageState.Waiting -> {
            Image(
                bitmap = thumbnail,
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        is ImageState.Success -> {
            ZoomableImage(
                bitmap = (cachedImage as ImageState.Success).data.asImageBitmap(),
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