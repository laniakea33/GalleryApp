package com.dh.galleryapp.feature.list

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dh.galleryapp.core.KeyGenerator
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.data.di.OnlineRepository
import com.dh.galleryapp.core.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.util.LinkedList
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @OnlineRepository private val repository: Repository,
    @ApplicationContext val context: Context,
) : ViewModel() {

    //  이미지 데이터 목록
    val images = repository.loadImageList()
        .cachedIn(viewModelScope)

    //  요청들을 관리하기 위한 collection
    private val jobs = HashMap<String, Job?>()

    private val memoryCache = HashMap<String, MutableStateFlow<CacheResult>>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0
    private val memoryCacheSizeMax: Long = 1024 * 1024 * 20 // 20mb

    private val diskCacheDir = context.externalCacheDir!!.absolutePath
    private val diskCacheKeyList = LinkedList<String>()
    private var diskCacheSize: Long = 0
    private val diskCacheSizeBytesMax: Long = 1024 * 1024 * 100 // 100 MB

    private val journalFileDir = context.filesDir
    private val journalFileName = "journal.txt"
    private val journalFilePath = "$journalFileDir/$journalFileName"

    init {
        diskCacheSize = getDiskCacheSize(diskCacheDir)
        Log.d("dhlog", "ListViewModel init() diskCacheSize $diskCacheSize")
        loadJournalFile(diskCacheKeyList)
    }

    private fun loadJournalFile(into: MutableList<String>) {
        into.addAll(repository.readLines(journalFilePath))

        Log.d("dhlog", "ListViewModel loadJournalFile() ${into.size}")

        val fileNames = repository.fileNames(diskCacheDir)

        val fakeKey = into.filter { key ->
            fileNames.none { name ->
                key == name
            }
        }

        into.removeAll(fakeKey.toSet())

        val fakeFileNames = fileNames.filter { fileName ->
            into.none { key ->
                key == fileName
            }
        }

        repository.deleteFiles(fakeFileNames)

        Log.d(
            "dhlog",
            "ListViewModel loadJournalFile() fakeKey : ${fakeKey.size}, fakeFiles : ${fakeFileNames.size}"
        )
    }

    fun requestImage(url: String): Job {
        val key = KeyGenerator.key(url)

        return CoroutineScope(Dispatchers.IO).launch {
            if (isLoading(key)) return@launch

            if (memoryCache[key] == null) {
                memoryCache[key] = MutableStateFlow(CacheResult.Loading)
            } else {
                memoryCache[key]!!.emit(CacheResult.Loading)
            }

            if (isCachedInMemory(key).also {
                    if (it) {
                        memoryLruCacheProcess(key, false)
                        diskLruCacheProcess(key, false)
                    }
                }) return@launch

            val filePath = "$diskCacheDir/$key"

            yield()

            if (isCachedInDisk(key)) {
                yield()

                onImageDownloaded(filePath, key, false)

            } else {
                val result = repository.downloadImage(url, filePath)

                if (result.isSuccess) {
                    try {
                        val fileSize = repository.fileLength(filePath)

                        diskLruCacheProcess(key, true, fileSize)

                        yield()

                        onImageDownloaded(filePath, key = key, true)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        memoryCache[key]?.emit(CacheResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        memoryCache[key]?.emit(CacheResult.Failure(it))
                    }
                }
            }
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

            if (isLoading(key)) return@launch

            if (memoryCache[key] == null) {
                memoryCache[key] = MutableStateFlow(CacheResult.Loading)
            } else {
                memoryCache[key]!!.emit(CacheResult.Loading)
            }

            if (isCachedInMemory(key).also {
                    if (it) {
                        memoryLruCacheProcess(key, false)
                        diskLruCacheProcess(key, false)
                    }
                }) return@launch

            Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 2")

            val orgFilePath = "$diskCacheDir/$originKey"
            val filePath = "$diskCacheDir/$key"

            yield()

            Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 3")

            if (isCachedInDisk(key)) {
                yield()

                Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 4")

                val bitmap = BitmapUtils.decode(filePath)!!
                memoryCache[key]?.emit(CacheResult.Success(bitmap))
                Log.d(
                    "dhlog",
                    "ListViewModel requestSampledImage() 디스크 캐시 있는 케이스, uiState 업데이트 함. key : $key"
                )
                memoryLruCacheProcess(key, true, bitmap.allocationByteCount.toLong())
                diskLruCacheProcess(key, false)

            } else if (isCachedInDisk(originKey)) {
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
                            "ListViewModel diskCacheSize size added $fileSize, total : $diskCacheSize"
                        )
                        diskLruCacheProcess(originKey, true, fileSize)

                        yield()

                        Log.d("dhlog", "ListViewModel requestSampledImage() : id$id point 4.5")

                        onImageDownloaded(orgFilePath, key, true, width, height)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        memoryCache[key]?.emit(CacheResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        memoryCache[key]?.emit(CacheResult.Failure(it))
                    }
                }
            }
        }.also {
            jobs[id] = it
        }
    }

    private fun isLoading(key: String): Boolean {
        return memoryCache[key] != null && memoryCache[key]!!.value is CacheResult.Loading
    }

    private fun isCachedInMemory(key: String): Boolean {
        return (memoryCache[key] != null && memoryCache[key]!!.value is CacheResult.Success).also {
            if (it) Log.d("dhlog", "ListViewModel isCachedInMemory() : $key")
        }
    }

    private fun isCachedInDisk(key: String): Boolean {
        val filePath = "$diskCacheDir/$key"
        return repository.fileExists(filePath).also {
            if (it) Log.d("dhlog", "ListViewModel isCachedInDisk() : $key")
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


        memoryCache[key]?.emit(CacheResult.Success(bitmap))
        memoryLruCacheProcess(key, true, bitmap.allocationByteCount.toLong())

        yield()

        if (sampling) {
            saveBitmapToDiskCache(key, bitmap)
        }

        if (isNewData) {
            val filePath = "$diskCacheDir/$key"
            val sampledFileSize = File(filePath).length()

            diskLruCacheProcess(key, true, sampledFileSize)
        } else {
            diskLruCacheProcess(key, false)
        }
    }

    private fun saveBitmapToDiskCache(fileName: String, bitmap: Bitmap) {
        repository.writeFileOutputStreamToFile(
            diskCacheDir,
            fileName,
            onFileOutputStream = {
                // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        )
    }

    fun observe(downloadUrl: String, width: Int = -1, height: Int = -1): StateFlow<CacheResult> {
        val key =
            if (width > 0 && height > 0) KeyGenerator.key(
                downloadUrl,
                width,
                height
            ) else KeyGenerator.key(downloadUrl)

        var flow = memoryCache[key]
        if (flow == null) {
            memoryCache[key] = MutableStateFlow(CacheResult.Waiting)
            flow = memoryCache[key]
        }

        return flow!!
    }

    private fun getDiskCacheSize(dirPath: String): Long {
        return repository.fileSizeSum(dirPath)
    }

    @Synchronized
    private fun memoryLruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0) {
        Log.d("dhlog", "ListViewModel memoryLruCacheProcess() : $key")

        if (isNewData) {
            memoryCacheSize += addedSize

            while (memoryCacheSize >= memoryCacheSizeMax) {
                removeLastMemoryCache()
            }
        }

        Log.d(
            "dhlog",
            "loadFromDiskToMemoryCache after size : ${memoryCache.size}, ${memoryCacheSize + addedSize} < $memoryCacheSizeMax"
        )

        memoryCacheKeyList.remove(key)
        memoryCacheKeyList.add(0, key)


        Log.d(
            "dhlog",
            "ListViewModel loadFromDiskToMemoryCache memoryCacheSize size added $addedSize, total : $memoryCacheSize"
        )
    }

    private fun removeLastMemoryCache() {
        val targetKey = memoryCacheKeyList.lastOrNull() ?: return
        if (memoryCache[targetKey] != null) {
            if (memoryCache[targetKey]!!.value is CacheResult.Success) {
                val target = (memoryCache[targetKey]!!.value as CacheResult.Success).data
                val targetSize = target.allocationByteCount
                memoryCache.remove(targetKey)
                memoryCacheSize -= targetSize
            }

            Log.d(
                "dhlog",
                "ListViewModel removeLastMemoryCache() memoryCacheSize size removed : ${memoryCache[targetKey]}"
            )
        }

        memoryCacheKeyList.remove(targetKey)
    }

    @Synchronized
    private fun diskLruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0) {
        Log.d("dhlog", "ListViewModel diskLruCacheProcess() : $key")
        if (isNewData) {
            diskCacheSize += addedSize

            while (diskCacheSize >= diskCacheSizeBytesMax) {
                removeLastDiskCache(addedSize)
            }
        }

        if (diskCacheKeyList.contains(key)) {
            diskCacheKeyList.remove(key)
        }

        repository.removeStringFromFile(journalFilePath, key)

        diskCacheKeyList.add(0, key)
        repository.prependStringToFile(journalFilePath, key)

        Log.d(
            "dhlog",
            "ListViewModel diskLruCacheProcess() : $key diskCacheKeyList : ${diskCacheKeyList.size} fin"
        )
    }

    private fun removeLastDiskCache(addedSize: Long) {
        val targetKey = diskCacheKeyList.lastOrNull() ?: return
        val targetFileName = "$diskCacheDir/$targetKey"

        Log.d(
            "dhlog",
            "ListViewModel diskLruCacheProcess() 와일문도는중 : $targetKey, targetSize : $addedSize"
        )

        if (repository.fileExists(targetFileName)) {
            val targetSize = repository.fileLength(targetFileName)
            repository.deleteFile(targetFileName)
            diskCacheSize -= targetSize
            Log.d("dhlog", "ListViewModel diskLruCacheProcess() 와일문도는중 : 실제로 파일이 삭제됨")
        }

        Log.d(
            "dhlog",
            "ListViewModel diskLruCacheProcess() 와일문도는중 : $targetKey, diskCacheSize : $diskCacheSize > $diskCacheSizeBytesMax"
        )

        diskCacheKeyList.remove(targetKey)

        Log.d(
            "dhlog",
            "ListViewModel diskLruCacheProcess() 와일문도는중 : 파일 삭제 됨 keySize :${diskCacheKeyList.size}, file size : ${
                File(diskCacheDir).listFiles()?.size
            }"
        )

        repository.removeStringFromFile(journalFilePath, targetKey)
    }

    fun requestImageWithKey(key: String): Bitmap {
        return BitmapUtils.decode("$diskCacheDir/$key")!!
    }

    fun cancelJob(id: String) {
        jobs[id]?.cancel()
    }
}

sealed class CacheResult {
    data class Success(val data: Bitmap) : CacheResult()
    data class Failure(val t: Throwable) : CacheResult()
    data object Loading : CacheResult()
    data object Waiting : CacheResult()
}