package com.dh.galleryapp.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Image(
    val id: String,
    val url: String,
    val downloadUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
)