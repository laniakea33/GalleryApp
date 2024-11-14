package com.dh.galleryapp.core.cache

import android.graphics.Bitmap
import android.util.Log
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.feature.list.CacheState
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor(
    private val repository: Repository,
) : Cache {

    private val cacheStateMap = HashMap<String, MutableStateFlow<CacheState>>()
    private val memoryCacheMap = HashMap<String, Bitmap>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0
    private val memoryCacheSizeMax: Long = 1024 * 1024 * 20 // 20mb

    fun isLoading(key: String): Boolean {
        return cacheStateMap[key] != null && cacheStateMap[key]!!.value is CacheState.Loading
    }

    override fun isCached(key: String): Boolean {
        return (memoryCacheMap[key] != null).also {
            if (it) Log.d("dhlog", "MemoryCache isCachedInMemory() : $key")
        }
    }

    suspend fun loadCachedImage(key: String) {
        update(key, CacheState.Success(memoryCacheMap[key]!!))
    }

    @Synchronized
    override fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        val beforeKeySize = memoryCacheKeyList.size
        val beforeMemoryCacheSize = memoryCacheMap.size

        Log.d("dhlog", "MemoryCache lruCacheProcess() start : $key")

        if (isNewData) {
            memoryCacheSize += addedSize

            while (memoryCacheSize >= memoryCacheSizeMax) {
                removeLastCache()
            }
        }

        memoryCacheKeyList.remove(key)
        memoryCacheKeyList.add(0, key)


//        Log.d(
//            "dhlog",
//            "MemoryCache loadFromDiskToMemoryCache memoryCacheSize size added $addedSize, total : $memoryCacheSize"
//        )


        val afterKeySize = memoryCacheKeyList.size
        val afterMemoryCacheSize = memoryCacheMap.size

        Log.d(
            "dhlog",
            "MemoryCache lruCacheProcess() : $beforeKeySize, $beforeMemoryCacheSize -> $afterKeySize, $afterMemoryCacheSize"
        )
        if (afterMemoryCacheSize != afterKeySize) {
            Log.d("dhlog", "MemoryCache lruCacheProcess() : 불일치 발생!!!!!!!")
        }

    }

    @Synchronized
    override fun removeLastCache() {
        val targetKey = memoryCacheKeyList.lastOrNull() ?: return

        memoryCacheMap[targetKey]?.let {
            val targetSize = it.allocationByteCount
            memoryCacheSize -= targetSize
        }

        memoryCacheMap.remove(targetKey)
        cacheStateMap.remove(targetKey)
        memoryCacheKeyList.remove(targetKey)
    }

    suspend fun update(key: String, cacheState: CacheState) {
        Log.d("dhlog", "MemoryCache update() : $key")
        if (cacheStateMap[key] == null) {
            cacheStateMap[key] = MutableStateFlow(cacheState)
        } else {
            if (cacheState is CacheState.Success && !memoryCacheKeyList.contains(key)) {
                memoryCacheKeyList.add(0, key)
                memoryCacheMap[key] = cacheState.data
            }

            cacheStateMap[key]!!.emit(cacheState)
        }
    }

    fun cacheFlow(key: String): MutableStateFlow<CacheState> {
//        Log.d("dhlog", "MemoryCache cacheFlow() : $key")
        var flow = cacheStateMap[key]
        if (flow == null) {
            cacheStateMap[key] = MutableStateFlow(CacheState.Waiting)
            flow = cacheStateMap[key]
        }

        return flow!!
    }

    fun disposeState(key: String) {
        Log.d("dhlog", "MemoryCache disposeState() : $key")
        cacheStateMap.remove(key)
    }
}