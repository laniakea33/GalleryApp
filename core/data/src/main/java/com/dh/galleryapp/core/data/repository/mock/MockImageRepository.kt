package com.dh.galleryapp.core.data.repository.mock

import android.graphics.BitmapFactory
import androidx.paging.PagingData
import com.dh.galleryapp.core.domain.OriginalImageResult
import com.dh.galleryapp.core.domain.ThumbnailImageResult
import com.dh.galleryapp.core.domain.repository.ImageRepository
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockImageRepository @Inject constructor() : ImageRepository {

    private val dummyImages = buildList {
        for (i in 0 until 10) {
            Image(
                id = i.toString(),
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3000,
                url = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/0/200/300",
            ).also {
                add(it)
            }
        }

        for (i in 0 until 10) {
            Image(
                id = i.toString(),
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3000,
                url = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/10/200/300",
            ).also {
                add(it)
            }
        }
    }

    override fun loadImageList(): Flow<PagingData<Image>> {
        return MutableStateFlow(PagingData.from(dummyImages))
    }

    override fun getSampledImage(
        url: String,
        width: Int,
        height: Int,
    ): Flow<ThumbnailImageResult> {
        return flow {
            emit(ThumbnailImageResult.Success(BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)))
        }
    }

    override fun getOriginalImage(thumbnailKey: String, url: String): Flow<OriginalImageResult> {
        return flow {
            emit(OriginalImageResult.Success(BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)))
        }
    }

    override suspend fun downloadImage(url: String, filePath: String): Result<String> {
        return Result.success(filePath)
    }
}