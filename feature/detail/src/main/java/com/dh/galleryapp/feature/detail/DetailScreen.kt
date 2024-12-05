package com.dh.galleryapp.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dh.galleryapp.core.ui.components.ZoomableImage
import com.dh.galleryapp.feature.model.ImageResult

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    url: String,
    thumbnailKey: String,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val imageResult by viewModel.imageResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestImage(thumbnailKey, url)
    }

    Box(
        modifier = modifier
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        DetailImage(
            imageResult = imageResult,
            modifier = modifier,
        )
    }
}

@Composable
private fun DetailImage(
    imageResult: ImageResult,
    modifier: Modifier = Modifier,
) {
    when (imageResult) {
        is ImageResult.Success -> {
            ZoomableImage(
                bitmap = (imageResult as ImageResult.Success).data.asImageBitmap(),
            )
        }

        is ImageResult.Failure -> {
            Text(
                text = (imageResult as ImageResult.Failure).t.message ?: "오류 발생",
                style = MaterialTheme.typography
                    .headlineMedium,
                modifier = modifier
                    .padding(8.dp)
                    .background(color = Color.LightGray),
            )
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}