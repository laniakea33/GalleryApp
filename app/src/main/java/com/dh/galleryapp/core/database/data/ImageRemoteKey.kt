package com.dh.galleryapp.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_remote_keys")
data class ImageRemoteKey(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prevPage: Int?,
    val nextPage: Int?
)