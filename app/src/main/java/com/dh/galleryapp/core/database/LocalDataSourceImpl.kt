package com.dh.galleryapp.core.database

import androidx.room.withTransaction
import com.dh.galleryapp.core.database.data.ImageRemoteKey
import com.dh.galleryapp.core.database.data.ImageResponse
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    private val dao: ImageDao,
    private val db: ImageDatabase,
) : LocalDataSource {

    override fun pagingSource() = dao.pagingSource()

    override fun getImages(): Result<List<ImageResponse>> {
        return try {
            val data = dao.getImages()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveImages(images: List<ImageResponse>) {
        dao.saveImages(images)
    }

    override suspend fun clearImages() {
        dao.clearImages()
    }

    override suspend fun saveRemoteKey(keys: ImageRemoteKey) {
        dao.saveRemoteKey(keys)
    }

    override suspend fun getRemoteKeyByImageId(id: Int) = dao.getRemoteKeyByImageId(id)

    override suspend fun clearRemoteKeys() {
        dao.clearRemoteKeys()
    }

    override suspend fun getLastRemoteKey() = dao.getLastRemoteKey()

    override suspend fun clearDataAndSaveImagesAndRemoteKey(
        images: List<ImageResponse>,
        keys: ImageRemoteKey,
    ) {
        db.withTransaction {
            dao.clearImages()
            dao.clearRemoteKeys()
            dao.saveImages(images)
            dao.saveRemoteKey(keys)
        }
    }

    override suspend fun saveImagesAndRemoteKey(images: List<ImageResponse>, keys: ImageRemoteKey) {
        db.withTransaction {
            dao.saveImages(images)
            dao.saveRemoteKey(keys)
        }
    }
}