package com.dh.galleryapp.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val minScale = 1f
    val maxScale = 10f

    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = Modifier
            .graphicsLayer(
                scaleX = zoom,
                scaleY = zoom,
                translationX = offsetX,
                translationY = offsetY,
            )
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(minScale, maxScale)
                        if (zoom > 1) {
                            val maxOffsetX = 197.82568f
                            val maxOffsetY = 197.82568f
                            offsetX += pan.x * zoom
                            offsetY += pan.y * zoom
                            offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                )
            }
            .fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}