package com.dh.galleryapp.core.domain

import com.dh.galleryapp.core.domain.repository.ImageRepository
import com.dh.galleryapp.core.domain.repository.di.Real
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOriginalImageUseCase @Inject constructor(
    @Real private val imageRepository: ImageRepository,
) {
    operator fun invoke(thumbnailKey: String, url: String): Flow<OriginalImageResult> {
        return imageRepository.getOriginalImage(thumbnailKey, url)
    }
}

sealed interface OriginalImageResult {
    data object Loading : OriginalImageResult
    data class Success(val data: Any) : OriginalImageResult
    data class Failure(val throwable: Throwable) : OriginalImageResult
}
