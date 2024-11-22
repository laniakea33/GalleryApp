package com.dh.galleryapp.feature.list

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.bitmapcache.BitmapCacheObject
import com.dh.galleryapp.core.cache.disk.DiskCache
import com.dh.galleryapp.core.cache.memory.MemoryCache
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.key.KeyGenerator
import com.dh.galleryapp.feature.list.model.KeyIndexMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private var imageStateMap = HashMap<String, MutableStateFlow<ImageState>>()
    private val mutex = Mutex()

    private val keyIndexMap = KeyIndexMap()

    fun requestImage(url: String) {
        val key = KeyGenerator.key(url)

        if (jobs[url]?.isActive == true) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isLoading(key)) return@launch

            updateState(key, ImageState.Loading)

            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!

                updateState(key, ImageState.Success(bitmap))

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

                        updateState(key, ImageState.Success(bitmap))

                        val filePath = "${diskCache.diskCacheDir}/$key"
                        val sampledFileSize = File(filePath).length()

                        diskCache.lruCacheProcess(key, true, sampledFileSize)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateState(key, ImageState.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        updateState(key, ImageState.Failure(it))
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

        if (jobs[key]?.isActive == true) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isLoading(key)) return@launch

            updateState(key, ImageState.Loading)

            if (memoryCache.isCached(key)) {
                val bitmap = memoryCache.cachedImage(key)!!.data as Bitmap
                updateState(key, ImageState.Success(bitmap))
                memoryCache.lruCacheProcess(key, false)
                diskCache.lruCacheProcess(key, false)
                return@launch
            }

            val orgFilePath = "${diskCache.diskCacheDir}/$originKey"
            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!
                updateState(key, ImageState.Success(bitmap))

                memoryCache.newCache(key, BitmapCacheObject(bitmap))
                memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())
                diskCache.lruCacheProcess(key, false)

            } else if (diskCache.isCached(key)) {
                onImageDownloaded(orgFilePath, key, false, width, height)

            } else {
                val result = repository.downloadImage(url, orgFilePath)

                if (result.isSuccess) {
                    try {
                        val orgFileName = result.getOrThrow()
                        val fileSize = repository.fileLength(orgFileName)

                        diskCache.lruCacheProcess(originKey, true, fileSize)

                        yield()

                        onImageDownloaded(orgFilePath, key, true, width, height)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateState(key, ImageState.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        updateState(key, ImageState.Failure(it))
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

        updateState(key, ImageState.Success(bitmap))

        memoryCache.newCache(key, BitmapCacheObject(bitmap))
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

    private suspend fun saveBitmapToDiskCache(fileName: String, bitmap: Bitmap) {
        diskCache.saveFileOutputStreamToDiskCache(
            fileName,
            onFileOutputStream = {
                // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        )
    }

    suspend fun observe(
        index: Int,
        downloadUrl: String,
        width: Int = -1,
        height: Int = -1,
    ): StateFlow<ImageState> {
        val key =
            if (width > 0 && height > 0) KeyGenerator.key(
                downloadUrl,
                width,
                height
            ) else KeyGenerator.key(downloadUrl)

        mutex.withLock {
            keyIndexMap.putOrUpdate(key, index, true)
        }

        return imageStateFlow(key)
    }

    suspend fun cancelRequest(index: Int, key: String) {
        jobs[key]?.cancel()
        jobs[key] = null

        mutex.withLock {
            keyIndexMap.putOrUpdate(key, index, false)
            val activeCount = keyIndexMap.getActiveCount(key)
            //  TODO active가 있는지 체크해서 캔슬 로직 실행
        }

        removeState(key)
    }

    private suspend fun imageStateFlow(key: String): MutableStateFlow<ImageState> {
        return mutex.withLock {
            var stateFlow = imageStateMap[key]

            if (stateFlow == null) {
                imageStateMap[key] = MutableStateFlow(ImageState.Waiting)
                stateFlow = imageStateMap[key]
            }
            stateFlow!!
        }
    }

    private suspend fun removeState(key: String) {
        mutex.withLock {
            imageStateMap.remove(key)
        }
    }

    private suspend fun isLoading(key: String): Boolean {
        return mutex.withLock {
            imageStateMap[key] != null && imageStateMap[key]!!.value is ImageState.Loading
        }
    }

    private suspend fun updateState(key: String, imageState: ImageState) {
        mutex.withLock {
            keyIndexMap.forEachActive(key) { index ->
                //  TODO 각 state에게 Notify
            }

            imageStateMap[key]?.emit(imageState)
        }
    }

    private suspend fun getImageState(key: String): MutableStateFlow<ImageState>? {
        return mutex.withLock {
            imageStateMap[key]
        }
    }

    fun requestImageWithKey(key: String): Bitmap {
        return BitmapUtils.decode("${diskCache.diskCacheDir}/$key")!!
    }
}

@Immutable
sealed class ImageState {
    data class Success(val data: Bitmap) : ImageState()
    data class Failure(val t: Throwable) : ImageState()
    data object Loading : ImageState()
    data object Waiting : ImageState()
}