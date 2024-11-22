package com.dh.galleryapp.feature.detail

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.cache.disk.DiskCache
import com.dh.galleryapp.core.cache.memory.MemoryCache
import com.dh.galleryapp.core.data.di.Real
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.key.KeyGenerator
import com.dh.galleryapp.feature.list.model.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    @Real private val repository: Repository,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache,
    @ApplicationContext val context: Context,
) : ViewModel() {

    private val _imageResult = MutableStateFlow<ImageResult>(ImageResult.Unknown)
    val imageResult: StateFlow<ImageResult> = _imageResult

    fun requestImage(thumbnailKey: String, url: String) {
        val key = KeyGenerator.key(url)

        viewModelScope.launch(Dispatchers.IO) {
            _imageResult.value = ImageResult.Loading

            if (memoryCache.isCached(thumbnailKey)) {
                val bitmap = memoryCache.cachedImage(thumbnailKey)!!.data as Bitmap
                _imageResult.value = ImageResult.Success(bitmap)
            }

            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!

                _imageResult.value = ImageResult.Success(bitmap)

                yield()

                diskCache.lruCacheProcess(key, false)

            } else {
                val result = repository.downloadImage(url, filePath)

                if (result.isSuccess) {
                    try {
                        val fileSize = repository.fileLength(filePath)

                        yield()

                        diskCache.lruCacheProcess(key, true, fileSize)

                        val bitmap = BitmapUtils.decode(filePath)!!

                        _imageResult.value = ImageResult.Success(bitmap)

                        val filePath = "${diskCache.diskCacheDir}/$key"
                        val sampledFileSize = File(filePath).length()

                        diskCache.lruCacheProcess(key, true, sampledFileSize)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        _imageResult.value = ImageResult.Failure(e)
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        _imageResult.value = ImageResult.Failure(it)
                    }
                }
            }
        }
    }
}