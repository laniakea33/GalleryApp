package com.dh.galleryapp.core.domain

import androidx.paging.PagingData
import com.dh.galleryapp.core.domain.repository.Repository
import com.dh.galleryapp.core.domain.repository.di.Real
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageListUseCase @Inject constructor(
    @Real private val repository: Repository,
) {
    operator fun invoke(): Flow<PagingData<Image>> {
        return repository.loadImageList()
    }
}