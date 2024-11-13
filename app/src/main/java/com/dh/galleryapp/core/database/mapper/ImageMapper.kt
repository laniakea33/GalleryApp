package com.dh.galleryapp.core.database.mapper

import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.database.data.ImageResponse

fun ImageResponse.toImage() = Image(
    id = id.toString(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl
)

fun Image.toImageResponse() = ImageResponse(
    id = id.toInt(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)

fun ImageResponse.toNetworkImageResponse() = com.dh.galleryapp.core.network.data.ImageResponse(
    id = id.toString(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)