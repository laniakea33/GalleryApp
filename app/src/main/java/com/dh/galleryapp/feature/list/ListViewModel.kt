package com.dh.galleryapp.feature.list

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dh.galleryapp.core.KeyGenerator
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.cache.DiskCache
import com.dh.galleryapp.core.cache.MemoryCache
import com.dh.galleryapp.core.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: Repository,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache,
    @ApplicationContext val context: Context,
) : ViewModel() {

    //  이미지 데이터 목록
    val images = repository.loadImageList()
        .cachedIn(viewModelScope)

    //  요청들을 관리하기 위한 collection
    private val jobs = HashMap<String, Job?>()

    fun requestImage(url: String): Job {
        Log.d("dhlog", "ListViewModel requestImage() called")
        val key = KeyGenerator.key(url)

        return CoroutineScope(Dispatchers.IO).launch {
            Log.d("dhlog", "ListViewModel requestImage() : ${memoryCache.cacheFlow(key).value}")

            if (memoryCache.isLoading(key)) return@launch

            memoryCache.update(key, CacheState.Loading)

            if (memoryCache.isCached(key).also {
                    if (it) {
                        memoryCache.loadCachedImage(key)
                        memoryCache.lruCacheProcess(key, false)
                        diskCache.lruCacheProcess(key, false)
                    }
                }) return@launch

            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                yield()

                onImageDownloaded(filePath, key, false)

            } else {
                val result = repository.downloadImage(url, filePath)

                if (result.isSuccess) {
                    try {
                        val fileSize = repository.fileLength(filePath)

                        diskCache.lruCacheProcess(key, true, fileSize)

                        yield()

                        onImageDownloaded(filePath, key = key, true)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        memoryCache.update(key, CacheState.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        memoryCache.update(key, CacheState.Failure(it))
                    }
                }
            }
        }.also {
            jobs[url] = it
        }
    }

    fun requestImageSampling(url: String, width: Int, height: Int, id: String) {
        val originKey = KeyGenerator.key(url)
        val key = KeyGenerator.key(url, width, height)

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(
                "dhlog",
                "ListViewModel requestSampledImage() : id$id $url, size : $width x $height, key : $key"
            )

            if (memoryCache.isLoading(key).also {
                    if (it) Log.d(
                        "dhlog",
                        "ListViewModel isLoading()"
                    )
                }) return@launch

            memoryCache.update(key, CacheState.Loading)

            if (memoryCache.isCached(key).also {
                    if (it) {
                        memoryCache.loadCachedImage(key)
                        memoryCache.lruCacheProcess(key, false)
                        diskCache.lruCacheProcess(key, false)
                    }
                }) return@launch

            Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 2")

            val orgFilePath = "${diskCache.diskCacheDir}/$originKey"
            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 3")

            if (diskCache.isCached(key)) {
                yield()

                Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 4")

                val bitmap = BitmapUtils.decode(filePath)!!
                memoryCache.update(key, CacheState.Success(bitmap))

                Log.d(
                    "dhlog",
                    "ListViewModel requestSampledImage() 디스크 캐시 있는 케이스, uiState 업데이트 함. key : $key"
                )

                memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())
                diskCache.lruCacheProcess(key, false)

            } else if (diskCache.isCached(key)) {
                Log.d("dhlog", "ListViewModel requestSampledImage() 원본파일만 있는 케이스 : $filePath")
                onImageDownloaded(orgFilePath, key, false, width, height)

            } else {
                val result = repository.downloadImage(url, orgFilePath)

                if (result.isSuccess) {
                    try {
                        val orgFileName = result.getOrThrow()
                        val fileSize = repository.fileLength(orgFileName)
                        Log.d(
                            "dhlog",
                            "ListViewModel diskCacheSize size added $fileSize, total : ${diskCache.diskCacheSize}"
                        )
                        diskCache.lruCacheProcess(originKey, true, fileSize)

                        yield()

                        Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 4.5")

                        onImageDownloaded(orgFilePath, key, true, width, height)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        memoryCache.update(key, CacheState.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        memoryCache.update(key, CacheState.Failure(it))
                    }
                }
            }
        }.also {
            jobs[key] = it
        }
    }

    private suspend fun onImageDownloaded(
        orgFilePath: String,
        key: String,
        isNewData: Boolean,
        width: Int = -1,
        height: Int = -1,
    ) {
        val sampling = width > 0 && height > 0

        val bitmap = if (sampling)
            BitmapUtils.decodeSample(orgFilePath, width, height)!!
        else BitmapUtils.decode(orgFilePath)!!

        yield()

        memoryCache.update(key, CacheState.Success(bitmap))

        memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())

        yield()

        if (sampling) {
            saveBitmapToDiskCache(key, bitmap)
        }

        if (isNewData) {
            val filePath = "${diskCache.diskCacheDir}/$key"
            val sampledFileSize = File(filePath).length()

            diskCache.lruCacheProcess(key, true, sampledFileSize)
        } else {
            diskCache.lruCacheProcess(key, false)
        }
    }

    private fun saveBitmapToDiskCache(fileName: String, bitmap: Bitmap) {
        diskCache.saveFileOutputStreamToDiskCache(
            fileName,
            onFileOutputStream = {
                // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        )
    }

    fun observe(downloadUrl: String, width: Int = -1, height: Int = -1): StateFlow<CacheState> {
        val key =
            if (width > 0 && height > 0) KeyGenerator.key(
                downloadUrl,
                width,
                height
            ) else KeyGenerator.key(downloadUrl)

        return memoryCache.cacheFlow(key)
    }

    fun requestImageWithKey(key: String): Bitmap {
        return BitmapUtils.decode("${diskCache.diskCacheDir}/$key")!!
    }

    fun cancelJob(key: String) {
        jobs[key]?.cancel()
        memoryCache.disposeState(key)
    }
}

@Immutable
sealed class CacheState {
    data class Success(val data: Bitmap) : CacheState()
    data class Failure(val t: Throwable) : CacheState()
    data object Loading : CacheState()
    data object Waiting : CacheState()
}