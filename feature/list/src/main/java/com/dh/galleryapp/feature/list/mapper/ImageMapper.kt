package com.dh.galleryapp.feature.list.mapper

import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.feature.model.ImageRequest

fun Image.toImageRequest() = ImageRequest(
    id = id,
    downloadUrl = downloadUrl,
)