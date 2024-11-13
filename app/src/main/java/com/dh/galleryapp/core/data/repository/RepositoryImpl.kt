package com.dh.galleryapp.core.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dh.galleryapp.core.data.repository.paging.ImageRemoteMediator
import com.dh.galleryapp.core.database.LocalDataSource
import com.dh.galleryapp.core.database.mapper.toImage
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.network.NetworkDataSource
import com.dh.galleryapp.core.storage.StorageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    private val imageRemoteMediator: ImageRemoteMediator,
    private val storageDataSource: StorageDataSource,
) : Repository {

    @OptIn(ExperimentalPagingApi::class)
    override fun loadImageList(): Flow<PagingData<Image>> {
        Log.d("dhlog", "RepositoryImpl loadImageList()")

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = true,
                initialLoadSize = pageSize * 3,
                prefetchDistance = pageSize / 2,
            ),
            pagingSourceFactory = { localDataSource.pagingSource() },
            remoteMediator = imageRemoteMediator,
        ).flow
            .map { pagingData ->
                pagingData.map { it.toImage() }
            }
    }

    override suspend fun downloadImage(url: String, filePath: String): Result<File> {
        return try {
            val responseBody = networkDataSource.downloadImage(url)

            val file = File(filePath)
            responseBody.byteStream().use { fileIn ->
                file.outputStream().use { fileOut ->
                    fileIn.copyTo(fileOut)
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}