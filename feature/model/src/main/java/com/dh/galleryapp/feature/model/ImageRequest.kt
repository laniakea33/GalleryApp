package com.dh.galleryapp.feature.model

import androidx.compose.runtime.Immutable

@Immutable
data class ImageRequest(
    val downloadUrl: String,
    val id: String,
)
