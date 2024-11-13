package com.dh.galleryapp.feature.list

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
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
import java.net.URLEncoder
import java.util.LinkedList
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @OnlineRepository private val repository: Repository,
    @ApplicationContext val context: Context,
) : ViewModel() {

    val images = repository.loadImageList()
        .cachedIn(viewModelScope)

    /*
    dir : context.externalCacheDir?.absolutePath
    fileName : [url.encode]_width_height.jpg
     */

    private val memoryCache = HashMap<String, MutableStateFlow<CacheResult>>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0
    private val memoryCacheSizeMax: Long = 1024 * 1024 * 20 // 20mb

    private val diskCacheKeyList = LinkedList<String>()
    private var diskCacheSize: Long = 0
    private val diskCacheSizeBytesMax: Long = 1024 * 1024 * 100 // 100 MB

    private val journalFileName = "journal.txt"

    private val jobs = HashMap<String, Job?>()

    init {
        diskCacheSize = getDiskCacheSize("${context.externalCacheDir?.absolutePath}")
        Log.d("dhlog", "ImageCache init() diskCacheSize $diskCacheSize")
        loadJournalFile(diskCacheKeyList)
    }

    private fun loadJournalFile(into: MutableList<String>) {
        val journalFile = File("${context.filesDir}/$journalFileName")
        if (!journalFile.exists()) {
            journalFile.createNewFile()
        } else {
            into.addAll(journalFile.readLines())
        }

        Log.d("dhlog", "ImageCache loadJournalFile() ${into.size}")

        val files = File("${context.externalCacheDir?.absolutePath}")
            .listFiles()
            ?.filter { it.isFile }

        if (files == null) return

        val fakeKey = into.filter { key ->
            files.none { file ->
                key == file.name
            }
        }

        into.removeAll(fakeKey.toSet())

        val fakeFiles = files.filter { file ->
            into.none { key ->
                key == file.name
            }
        }

        fakeFiles.forEach {
            it.delete()
        }

        Log.d(
            "dhlog",
            "ImageCache loadJournalFile() fakeKey : ${fakeKey.size}, fakeFiles : ${fakeFiles.size}"
        )
    }

    fun requestImage(url: String): Job {
        val key = "${URLEncoder.encode(url)}.jpg"

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

            val filePath = "${context.externalCacheDir?.absolutePath}/$key"

            yield()

            if (isCachedInDisk(filePath)) {
                yield()

                onImageDownloaded(filePath, key, false)

            } else {
                val result = repository.downloadImage(url, filePath)

                if (result.isSuccess) {
                    try {
                        val file = result.getOrThrow()
                        val fileSize = file.length()

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
        val originKey = "${URLEncoder.encode(url)}.jpg"
        val key = "${URLEncoder.encode(url)}_${width}_$height.jpg"

        val job = CoroutineScope(Dispatchers.IO).launch {
            Log.d(
                "dhlog",
                "ImageCache requestSampledImage() : id$id $url, size : $width x $height, key : $key"
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

            Log.d("dhlog", "ImageCache requestSampledImage() : id$id point 2")

            val orgFilePath = "${context.externalCacheDir?.absolutePath}/$originKey"
            val filePath = "${context.externalCacheDir?.absolutePath}/$key"

            yield()

            Log.d("dhlog", "ImageCache requestSampledImage() : id$id point 3")

            if (isCachedInDisk(filePath)) {
                yield()

                Log.d("dhlog", "ImageCache requestSampledImage() : id$id point 4")

                val bitmap = BitmapUtils.decode(filePath)!!
                memoryCache[key]?.emit(CacheResult.Success(bitmap))
                Log.d(
                    "dhlog",
                    "ImageCache requestSampledImage() 디스크 캐시 있는 케이스, uiState 업데이트 함. key : $key"
                )
                memoryLruCacheProcess(key, true, bitmap.allocationByteCount.toLong())
                diskLruCacheProcess(key, false)

            } else if (isCachedInDisk(orgFilePath)) {
                Log.d("dhlog", "ImageCache requestSampledImage() 원본파일만 있는 케이스 : $filePath")
                onImageDownloaded(orgFilePath, key, false, width, height)

            } else {
                val result = repository.downloadImage(url, orgFilePath)

                if (result.isSuccess) {
                    try {
                        val orgFile = result.getOrThrow()
                        val fileSize = orgFile.length()
                        Log.d(
                            "dhlog",
                            "ImageCache diskCacheSize size added $fileSize, total : $diskCacheSize"
                        )
                        diskLruCacheProcess(originKey, true, fileSize)

                        yield()

                        Log.d("dhlog", "ImageCache requestSampledImage() : id$id point 4.5")

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
            if (it) Log.d("dhlog", "ImageCache isCachedInMemory() : $key")
        }
    }

    private fun isCachedInDisk(filePath: String): Boolean {
        val cacheFile = File(filePath)
        return cacheFile.exists().also {
            if (it) Log.d("dhlog", "ImageCache isCachedInDisk() : ${cacheFile.name}")
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
            BitmapUtils.saveBitmapToFile(
                bitmap,
                context.externalCacheDir!!.absolutePath,
                key
            )
        }

        if (isNewData) {
            val filePath = "${context.externalCacheDir?.absolutePath}/$key"
            val sampledFileSize = File(filePath).length()

            diskLruCacheProcess(key, isNewData, sampledFileSize)
        } else {
            diskLruCacheProcess(key, isNewData)
        }
    }

    fun observe(downloadUrl: String, width: Int = -1, height: Int = -1): StateFlow<CacheResult> {
        val key =
            if (width > 0 && height > 0) "${URLEncoder.encode(downloadUrl)}_${width}_$height.jpg" else "${
                URLEncoder.encode(downloadUrl)
            }.jpg"

        var flow = memoryCache[key]
        if (flow == null) {
            memoryCache[key] = MutableStateFlow(CacheResult.Waiting)
            flow = memoryCache[key]
        }

        return flow!!
    }

    fun observeOriginal(downloadUrl: String): StateFlow<CacheResult> {
        val fileName = "${URLEncoder.encode(downloadUrl)}.jpg"
        var flow = memoryCache[fileName]

        if (flow == null) {
            Log.d("dhlog", "ImageCache observe() : null")

            memoryCache[fileName] = MutableStateFlow(CacheResult.Waiting)
            flow = memoryCache[fileName]
        } else {
            when (flow.value) {
                is CacheResult.Failure -> {
                    Log.d("dhlog", "ImageCache observe() : failure")
                }

                CacheResult.Loading -> {
                    Log.d("dhlog", "ImageCache observe() : loading")
                }

                is CacheResult.Success -> {
                    Log.d(
                        "dhlog",
                        "ImageCache observe() : success, isRecycled :${(flow.value as CacheResult.Success).data.isRecycled}"
                    )
                }

                CacheResult.Waiting -> {
                    Log.d("dhlog", "ImageCache observe() : waiting")
                }
            }

        }

        return flow!!
    }

    private fun getDiskCacheSize(dirPath: String): Long {
        var sizeSum = 0L
        File(dirPath)
            .listFiles()
            ?.filter { it.isFile }
            ?.let { list ->
                list.forEach { sizeSum += it.length() }
            }
        return sizeSum
    }

    @Synchronized
    private fun memoryLruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0) {
        Log.d("dhlog", "ImageCache memoryLruCacheProcess() : $key")

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
            "ImageCache loadFromDiskToMemoryCache memoryCacheSize size added $addedSize, total : $memoryCacheSize"
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
                "ImageCache removeLastMemoryCache() memoryCacheSize size removed : ${memoryCache[targetKey]}"
            )
        }

        memoryCacheKeyList.remove(targetKey)
    }

    @Synchronized
    private fun diskLruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0) {
        Log.d("dhlog", "ImageCache diskLruCacheProcess() : $key")
        if (isNewData) {
            diskCacheSize += addedSize

            while (diskCacheSize >= diskCacheSizeBytesMax) {
                val targetKey = diskCacheKeyList.lastOrNull() ?: continue
                val targetFile = File("${context.externalCacheDir?.absolutePath}/$targetKey")

                Log.d(
                    "dhlog",
                    "ImageCache diskLruCacheProcess() 와일문도는중 : $targetKey, targetSize : $addedSize"
                )

                if (targetFile.exists()) {
                    val targetSize = targetFile.length()
                    targetFile.delete()
                    diskCacheSize -= targetSize
                    Log.d("dhlog", "ImageCache diskLruCacheProcess() 와일문도는중 : 실제로 파일이 삭제됨")
                }

                Log.d(
                    "dhlog",
                    "ImageCache diskLruCacheProcess() 와일문도는중 : $targetKey, diskCacheSize : $diskCacheSize > $diskCacheSizeBytesMax"
                )

                diskCacheKeyList.remove(targetKey)

                Log.d(
                    "dhlog",
                    "ImageCache diskLruCacheProcess() 와일문도는중 : 파일 삭제 됨 keySize :${diskCacheKeyList.size}, file size : ${
                        File("${context.externalCacheDir?.absolutePath}").listFiles()?.size
                    }"
                )

                removeStringFromFile("${context.filesDir}/$journalFileName", targetKey)
            }
        }

        if (diskCacheKeyList.contains(key)) {
            diskCacheKeyList.remove(key)
        }

        removeStringFromFile("${context.filesDir}/$journalFileName", key)

        diskCacheKeyList.add(0, key)
        addStringToFile("${context.filesDir}/$journalFileName", key)

        Log.d(
            "dhlog",
            "ImageCache diskLruCacheProcess() : $key diskCacheKeyList : ${diskCacheKeyList.size} fin"
        )
    }

    @Synchronized
    private fun removeStringFromFile(journalFilePath: String, targetString: String) {
        val file = File(journalFilePath)
        val lines = file.readLines()    //  개행문자를 제외하고 List<String>을 반환

        val modifiedLine = lines.mapIndexed { index, st ->
            if (st.contains(targetString)) {
                st.replace(targetString, "")
            } else st
        }

        val bufferedWrite = file.bufferedWriter()
        modifiedLine.forEachIndexed { index, st ->
            if (st.isNotBlank()) {
                if (index < modifiedLine.size - 1) {
                    bufferedWrite.write(st + "\n")
                } else {
                    bufferedWrite.write(st)
                }
            }
        }
        bufferedWrite.flush()
        bufferedWrite.close()
    }

    @Synchronized
    private fun addStringToFile(journalFilePath: String, targetString: String) {
        Log.d("dhlog", "addStringToFile << $targetString at ${Thread.currentThread().name}")
        val file = File(journalFilePath)
        val lines = file.readLines()
        val bufferedWrite = file.bufferedWriter()

        if (lines.isNotEmpty()) {
            bufferedWrite.write(targetString + "\n")
            lines.forEachIndexed { i, item ->
                if (i < lines.size - 1) {
                    bufferedWrite.write(item + "\n")
                } else {
                    bufferedWrite.write(item)
                }
            }
        } else {
            bufferedWrite.write(targetString)
        }

        bufferedWrite.flush()
        bufferedWrite.close()
    }

    fun requestImageWithKey(key: String): Bitmap {
        val file = File(context.externalCacheDir?.absolutePath + "/" + key)
        return BitmapUtils.decode(file.absolutePath)!!
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