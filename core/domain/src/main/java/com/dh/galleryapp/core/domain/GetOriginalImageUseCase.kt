package com.dh.galleryapp.core.domain

import com.dh.galleryapp.core.domain.repository.Repository
import com.dh.galleryapp.core.domain.repository.di.Real
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOriginalImageUseCase @Inject constructor(
    @Real private val repository: Repository,
) {
    operator fun invoke(thumbnailKey: String, url: String): Flow<OriginalImageResult> {
        return repository.getOriginalImage(thumbnailKey, url)
    }
}

sealed interface OriginalImageResult {
    data object Loading : OriginalImageResult
    data class Success(val data: Any) : OriginalImageResult
    data class Failure(val throwable: Throwable) : OriginalImageResult
}
