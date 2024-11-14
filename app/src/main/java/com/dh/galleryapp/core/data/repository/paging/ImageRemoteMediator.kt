package com.dh.galleryapp.core.data.repository.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dh.galleryapp.core.data.repository.pageSize
import com.dh.galleryapp.core.database.LocalDataSource
import com.dh.galleryapp.core.database.data.ImageRemoteKey
import com.dh.galleryapp.core.database.data.ImageResponse
import com.dh.galleryapp.core.network.NetworkDataSource
import com.dh.galleryapp.core.network.mapper.toDatabaseImageResponse
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ImageRemoteMediator @Inject constructor(
    private val network: NetworkDataSource,
    private val local: LocalDataSource,
) : RemoteMediator<Int, ImageResponse>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ImageResponse>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> local.getLastRemoteKey()?.nextPage ?: return MediatorResult.Success(
                endOfPaginationReached = true
            )
        }

        val result = try {
            val images = network.loadImageList(page, pageSize)

            val endOfPaginationReached = images.size < pageSize

            val prevPage = if (page == 1) null else page - 1
            val nextPage = if (endOfPaginationReached) null else page + 1

            val key = ImageRemoteKey(prevPage = prevPage, nextPage = nextPage)

            if (loadType == LoadType.REFRESH) {
                local.clearDataAndSaveImagesAndRemoteKey(
                    images.map { it.toDatabaseImageResponse() },
                    key
                )
            } else {
                local.saveImagesAndRemoteKey(images.map { it.toDatabaseImageResponse() }, key)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }

        return result
    }
}