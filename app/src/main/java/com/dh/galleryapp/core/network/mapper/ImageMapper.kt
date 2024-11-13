package com.dh.galleryapp.core.network.mapper

import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.network.data.ImageResponse

fun ImageResponse.toImage() = Image(
    id = id,
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl
)

fun Image.toImageResponse() = ImageResponse(
    id = id,
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)

fun ImageResponse.toDatabaseImageResponse() = com.dh.galleryapp.core.database.data.ImageResponse(
    id = id.toInt(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)