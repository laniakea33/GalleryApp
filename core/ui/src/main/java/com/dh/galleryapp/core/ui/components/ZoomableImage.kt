package com.dh.galleryapp.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val minScale = 1f
    val maxScale = 10f

    var composableSize by remember { mutableStateOf(IntSize.Zero) }


    val imageSize by rememberUpdatedState {
        val ratio = if (composableSize.width > composableSize.height) {
            bitmap.height.toFloat() / composableSize.height
        } else {
            bitmap.width.toFloat() / composableSize.width
        }

        IntSize(
            width = (bitmap.width.toFloat() / ratio).toInt(),
            height = (bitmap.height.toFloat() / ratio).toInt()
        )
    }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                composableSize = it.size
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(minScale, maxScale)
                        if (zoom > 1) {
                            val imageSizeValue = imageSize()
                            val width = imageSizeValue.width
                            val height = imageSizeValue.height
                            val zoomedWidth = width.toFloat() * zoom
                            val zoomedHeight = height.toFloat() * zoom

                            val maxOffsetX =
                                (zoomedWidth - composableSize.width.toFloat()) / 2f
                            val maxOffsetY =
                                (zoomedHeight - composableSize.height.toFloat()) / 2f

                            offsetX += pan.x
                            offsetY += pan.y
                            offsetX =
                                if (composableSize.width > zoomedWidth) 0f else offsetX.coerceIn(
                                    -maxOffsetX,
                                    maxOffsetX
                                )
                            offsetY =
                                if (composableSize.height > zoomedHeight) 0f else offsetY.coerceIn(
                                    -maxOffsetY,
                                    maxOffsetY
                                )

                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                )
            }
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = offsetX
                translationY = offsetY
            }
    )
}
