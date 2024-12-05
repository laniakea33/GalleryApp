package com.dh.galleryapp.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageResponse(
    @PrimaryKey val id: Int,
    val url: String,
    val downloadUrl: String,
    val width: Int,
    val height: Int,
    val author: String,
)