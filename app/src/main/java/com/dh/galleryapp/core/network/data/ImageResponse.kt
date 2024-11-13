package com.dh.galleryapp.core.network.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageResponse(
    val id: String,
    val url: String,
    @Json(name = "download_url") val downloadUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
)