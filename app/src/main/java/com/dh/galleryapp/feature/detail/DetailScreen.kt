package com.dh.galleryapp.feature.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.dh.galleryapp.core.imagecache.CacheResult
import com.dh.galleryapp.core.imagecache.ImageCache
import com.dh.galleryapp.core.ui.components.ZoomableImage
import com.dh.galleryapp.core.ui.components.rememberImageCache

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    url: String,
    thumbnailKey: String,
    imageCache: ImageCache = rememberImageCache(),
) {
    LaunchedEffect(url) {
        imageCache.requestImage(url)
    }

    Box(
        modifier = modifier
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val cachedImage by imageCache.observe(url)
            .collectAsState(CacheResult.Loading)

        when (cachedImage) {
            CacheResult.Loading, CacheResult.Waiting -> {
                Image(
                    bitmap = imageCache.requestImageWithKey(thumbnailKey).asImageBitmap(),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.Fit
                )
            }

            is CacheResult.Success -> {
                ZoomableImage(
                    bitmap = (cachedImage as CacheResult.Success).data.asImageBitmap(),
                    modifier = modifier,
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
}