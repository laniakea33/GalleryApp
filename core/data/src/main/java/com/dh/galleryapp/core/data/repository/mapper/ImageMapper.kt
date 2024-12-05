package com.dh.galleryapp.core.data.repository.mapper

import com.dh.galleryapp.core.model.Image

fun com.dh.galleryapp.core.network.data.ImageResponse.toImage() = Image(
    id = id,
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl
)

fun com.dh.galleryapp.core.database.data.ImageResponse.toImage() = Image(
    id = id.toString(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl
)

fun Image.toNetworkImageResponse() = com.dh.galleryapp.core.network.data.ImageResponse(
    id = id,
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)

fun Image.toDatabaseImageResponse() = com.dh.galleryapp.core.database.data.ImageResponse(
    id = id.toInt(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)

fun com.dh.galleryapp.core.database.data.ImageResponse.toNetworkImageResponse() = com.dh.galleryapp.core.network.data.ImageResponse(
    id = id.toString(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)

fun com.dh.galleryapp.core.network.data.ImageResponse.toDatabaseImageResponse() = com.dh.galleryapp.core.database.data.ImageResponse(
    id = id.toInt(),
    author = author,
    width = width,
    height = height,
    url = url,
    downloadUrl = downloadUrl,
)