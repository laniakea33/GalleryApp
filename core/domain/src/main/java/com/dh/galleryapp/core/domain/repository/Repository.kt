package com.dh.galleryapp.core.domain.repository

import androidx.paging.PagingData
import com.dh.galleryapp.core.domain.OriginalImageResult
import com.dh.galleryapp.core.domain.ThumbnailImageResult
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

interface Repository {

    fun loadImageList(): Flow<PagingData<Image>>
    suspend fun downloadImage(url: String, filePath: String): Result<String>
    suspend fun getSampledImage(
        url: String,
        width: Int,
        height: Int,
    ): Flow<ThumbnailImageResult>
    fun getOriginalImage(thumbnailKey: String, url: String): Flow<OriginalImageResult>

    fun createFile(filePath: String): File
    fun readLines(filePath: String): List<String>
    fun fileNames(dirPath: String): List<String>
    fun deleteFile(filePath: String): Boolean
    fun deleteFiles(filePathList: List<String>)
    fun prependStringToFile(filePath: String, s: String)
    fun removeStringFromFile(filePath: String, s: String)
    fun fileExists(filePath: String): Boolean
    fun fileLength(filePath: String): Long
    fun fileSizeSum(dirPath: String): Long
    fun writeFileOutputStreamToFile(
        dirPath: String,
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit,
    )
}
