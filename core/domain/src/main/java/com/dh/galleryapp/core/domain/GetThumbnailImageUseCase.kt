package com.dh.galleryapp.core.domain

import com.dh.galleryapp.core.domain.repository.ImageRepository
import com.dh.galleryapp.core.domain.repository.di.Real
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThumbnailImageUseCase @Inject constructor(
    @Real private val imageRepository: ImageRepository,
) {

    operator fun invoke(
        url: String,
        width: Int,
        height: Int,
    ): Flow<ThumbnailImageResult> {
        return imageRepository.getSampledImage(
            url = url, width = width, height = height,
        )
    }
}

sealed interface ThumbnailImageResult {
    data object Loading : ThumbnailImageResult
    data class Success(val data: Any) : ThumbnailImageResult
    data class Failure(val throwable: Throwable) : ThumbnailImageResult
}
