package com.dh.galleryapp.core.domain.repository

import androidx.paging.PagingData
import com.dh.galleryapp.core.domain.OriginalImageResult
import com.dh.galleryapp.core.domain.ThumbnailImageResult
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow

interface ImageRepository {

    fun loadImageList(): Flow<PagingData<Image>>
    suspend fun downloadImage(url: String, filePath: String): Result<String>
    fun getSampledImage(
        url: String,
        width: Int,
        height: Int,
    ): Flow<ThumbnailImageResult>

    fun getOriginalImage(thumbnailKey: String, url: String): Flow<OriginalImageResult>
}
