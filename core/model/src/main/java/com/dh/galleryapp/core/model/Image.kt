package com.dh.galleryapp.core.model

data class Image(
    val id: String,
    val url: String,
    val downloadUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
)