package com.dh.galleryapp.feature.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dh.galleryapp.core.domain.GetOriginalImageUseCase
import com.dh.galleryapp.core.domain.OriginalImageResult
import com.dh.galleryapp.feature.key.KeyGenerator
import com.dh.galleryapp.feature.model.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    val getOriginalImageUseCase: GetOriginalImageUseCase,
) : ViewModel() {

    private val _imageResult = MutableStateFlow<ImageResult>(ImageResult.Unknown)
    val imageResult: StateFlow<ImageResult> = _imageResult

    fun requestImage(thumbnailKey: String, url: String) {
        val key = KeyGenerator.key(url)

        val ceh = CoroutineExceptionHandler { c, t ->
            t.printStackTrace()
            viewModelScope.launch(Dispatchers.IO) {
                _imageResult.value = ImageResult.Failure(t)
            }
        }

        viewModelScope.launch(Dispatchers.IO + ceh) {
            getOriginalImageUseCase(thumbnailKey = thumbnailKey, url = url).collect { result ->
                when (result) {
                    OriginalImageResult.Loading -> {
                        _imageResult.value = ImageResult.Loading
                    }

                    is OriginalImageResult.Success -> {
                        _imageResult.value = ImageResult.Success(result.data as Bitmap)
                    }

                    is OriginalImageResult.Failure -> {
                        val e = result.throwable
                        e.printStackTrace()
                        _imageResult.value = ImageResult.Failure(e)
                    }
                }
            }
        }
    }
}
