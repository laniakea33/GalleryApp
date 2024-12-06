package com.dh.galleryapp.feature.list

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.dh.galleryapp.core.domain.GetImageListUseCase
import com.dh.galleryapp.core.domain.GetThumbnailImageUseCase
import com.dh.galleryapp.core.domain.ThumbnailImageResult
import com.dh.galleryapp.feature.key.KeyGenerator
import com.dh.galleryapp.feature.list.keystatusmap.KeyResultNotifier
import com.dh.galleryapp.feature.list.mapper.toImageRequest
import com.dh.galleryapp.feature.model.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    getImageListUseCase: GetImageListUseCase,
    private val getThumbnailImageUseCase: GetThumbnailImageUseCase,
) : ViewModel() {

    //  이미지 요청 데이터 목록
    val imageRequestList = getImageListUseCase()
        .map { pagingData ->
            pagingData.map {
                it.toImageRequest()
            }
        }
        .cachedIn(viewModelScope)

    //  요청들을 관리하기 위한 collection.
    private val jobs = HashMap<String, Job?>()

    //  이미지 응답 상태
    //  observe하면 새로 추가됨. 이미 있으면 덮어쓰기, dispose되면 Unknown으로 세팅됨.
    //  observe할 떄 keyImageResultMap에서 상태를 가져옴
    val imageResultList = mutableStateListOf<ImageResult>()

    private val keyResultNotifier = KeyResultNotifier()

    private val mutex = Mutex()

    fun requestImageSampling(url: String, width: Int, height: Int) {
        val key = KeyGenerator.key(url, width, height)

        if (jobs[key]?.isActive == true) return

        val ceh = CoroutineExceptionHandler { c, t ->
            t.printStackTrace()
            viewModelScope.launch(Dispatchers.IO) {
                updateImageResult(key, ImageResult.Failure(t))
            }
        }

        viewModelScope.launch(Dispatchers.IO + ceh) {
            if (noNeedToLoad(key)) return@launch

            getThumbnailImageUseCase(
                url = url,
                width = width,
                height = height
            ).collect { result ->
                when (result) {
                    ThumbnailImageResult.Loading -> {
                        updateImageResult(key, ImageResult.Loading)
                    }

                    is ThumbnailImageResult.Success -> {
                        updateImageResult(key, ImageResult.Success(result.data as Bitmap))
                    }

                    is ThumbnailImageResult.Failure -> {
                        val e = result.throwable
                        e.printStackTrace()
                        updateImageResult(
                            key,
                            ImageResult.Failure(e)
                        )
                    }
                }
            }

        }.also {
            jobs[key] = it
        }
    }

    suspend fun observe(
        index: Int,
        downloadUrl: String,
        width: Int = -1,
        height: Int = -1,
    ) {
        val key =
            if (width > 0 && height > 0) KeyGenerator.key(
                downloadUrl,
                width,
                height
            ) else KeyGenerator.key(downloadUrl)

        mutex.withLock {
            keyResultNotifier.observe(key, index)
            val imageResult = keyResultNotifier.getImageResult(key)!!

            val curList = imageResultList
            if (curList.getOrNull(index) == null) {
                for (i in curList.size until index) {
                    imageResultList.add(ImageResult.Unknown)
                }
                imageResultList.add(imageResult)

            } else {
                imageResultList[index] = imageResult
            }
        }
    }

    suspend fun dispose(index: Int, key: String) {
        mutex.withLock {
            keyResultNotifier.dispose(key, index)

            if (keyResultNotifier.hasNoObserver(key)) {
                jobs[key]?.cancel()
                jobs[key] = null
            }

            if (imageResultList.getOrNull(index) != null) {
                imageResultList[index] = ImageResult.Unknown
            }
        }
    }

    private suspend fun noNeedToLoad(key: String): Boolean {
        return mutex.withLock {
            val imageResult = keyResultNotifier.getImageResult(key)
            imageResult != null && (imageResult is ImageResult.Loading || imageResult is ImageResult.Success)
        }
    }

    private suspend fun updateImageResult(
        key: String,
        imageResult: ImageResult,
    ) {
        mutex.withLock {
            keyResultNotifier.updateImageResult(key, imageResult) {
                imageResultList[it] = imageResult
            }
        }
    }
}