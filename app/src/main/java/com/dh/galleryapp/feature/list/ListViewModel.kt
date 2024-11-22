package com.dh.galleryapp.feature.list

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.bitmapcache.BitmapCacheObject
import com.dh.galleryapp.core.cache.disk.DiskCache
import com.dh.galleryapp.core.cache.memory.MemoryCache
import com.dh.galleryapp.core.data.di.Real
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.key.KeyGenerator
import com.dh.galleryapp.feature.list.keystatusmap.KeyResultNotifier
import com.dh.galleryapp.feature.list.model.ImageRequest
import com.dh.galleryapp.feature.list.model.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @Real private val repository: Repository,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache,
    @ApplicationContext val context: Context,
) : ViewModel() {

    //  이미지 요청 데이터 목록
    val imageRequestList = repository.loadImageList()
        .map { pagingData ->
            pagingData.map {
                ImageRequest(
                    downloadUrl = it.downloadUrl,
                    id = it.id
                )
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

    fun requestImage(url: String) {
        val key = KeyGenerator.key(url)

        if (jobs[url]?.isActive == true) return

        viewModelScope.launch(Dispatchers.IO) {
            if (noNeedToLoad(key)) return@launch

            updateImageResult(key, ImageResult.Loading)

            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!

                updateImageResult(key, ImageResult.Success(bitmap))

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

                        updateImageResult(key, ImageResult.Success(bitmap))

                        val filePath = "${diskCache.diskCacheDir}/$key"
                        val sampledFileSize = File(filePath).length()

                        diskCache.lruCacheProcess(key, true, sampledFileSize)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateImageResult(key, ImageResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        updateImageResult(key, ImageResult.Failure(it))
                    }
                }
            }
        }.also {
            jobs[url] = it
        }
    }

    fun requestImageSampling(url: String, width: Int, height: Int, index: Int) {
        val originKey = KeyGenerator.key(url)
        val key = KeyGenerator.key(url, width, height)

        if (jobs[key]?.isActive == true) return

        viewModelScope.launch(Dispatchers.IO) {
            if (noNeedToLoad(key)) return@launch

            updateImageResult(key, ImageResult.Loading)

            if (memoryCache.isCached(key)) {
                val bitmap = memoryCache.cachedImage(key)!!.data as Bitmap
                updateImageResult(key, ImageResult.Success(bitmap))
                memoryCache.lruCacheProcess(key, false)
                diskCache.lruCacheProcess(key, false)
                return@launch
            }

            val orgFilePath = "${diskCache.diskCacheDir}/$originKey"
            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!
                updateImageResult(key, ImageResult.Success(bitmap))

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
                        updateImageResult(key, ImageResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        updateImageResult(key, ImageResult.Failure(it))
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

        updateImageResult(key, ImageResult.Success(bitmap))

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

    private suspend fun updateImageResult(key: String, imageResult: ImageResult) {
        mutex.withLock {
            keyResultNotifier.updateImageResult(key, imageResult) {
                imageResultList[it] = imageResult
            }
        }
    }
}