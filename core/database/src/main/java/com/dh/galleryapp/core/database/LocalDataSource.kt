package com.dh.galleryapp.core.database

import androidx.paging.PagingSource
import com.dh.galleryapp.core.database.data.ImageRemoteKey
import com.dh.galleryapp.core.database.data.ImageResponse

interface LocalDataSource {

    fun pagingSource(): PagingSource<Int, ImageResponse>
    fun getImages(): Result<List<ImageResponse>>
    suspend fun saveImages(images: List<ImageResponse>)
    suspend fun clearImages()
    suspend fun saveRemoteKey(keys: ImageRemoteKey)
    suspend fun getRemoteKeyByImageId(id: Int): ImageRemoteKey?
    suspend fun clearRemoteKeys()
    suspend fun getLastRemoteKey(): ImageRemoteKey?

    suspend fun clearDataAndSaveImagesAndRemoteKey(images: List<ImageResponse>, keys: ImageRemoteKey)
    suspend fun saveImagesAndRemoteKey(images: List<ImageResponse>, keys: ImageRemoteKey)
}