package com.dh.galleryapp.core.data.repository

import androidx.paging.PagingData
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow
import java.io.File

interface Repository {

    fun loadImageList(): Flow<PagingData<Image>>
    suspend fun downloadImage(url: String, filePath: String): Result<File>
}