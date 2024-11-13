package com.dh.galleryapp.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dh.galleryapp.core.data.di.OnlineRepository
import com.dh.galleryapp.core.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @OnlineRepository private val repository: Repository,
) : ViewModel() {

    val images = repository.loadImageList()
        .cachedIn(viewModelScope)
}