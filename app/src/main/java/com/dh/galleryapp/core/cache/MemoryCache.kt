package com.dh.galleryapp.core.cache

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

    private val memoryCacheMap = HashMap<String, MutableStateFlow<CacheState>>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0
    private val memoryCacheSizeMax: Long = 1024 * 1024 * 20 // 20mb

    fun isLoading(key: String): Boolean {
        return memoryCacheMap[key] != null && memoryCacheMap[key]!!.value is CacheState.Loading
    }

    override fun isCached(key: String): Boolean {
        return (memoryCacheMap[key] != null && memoryCacheMap[key]!!.value is CacheState.Success).also {
            if (it) Log.d("dhlog", "MemoryCache isCachedInMemory() : $key")
        }
    }

    @Synchronized
    override fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        Log.d("dhlog", "MemoryCache memoryLruCacheProcess() : $key")

        if (isNewData) {
            memoryCacheSize += addedSize

            while (memoryCacheSize >= memoryCacheSizeMax) {
                removeLastCache()
            }
        }

        Log.d(
            "dhlog",
            "MemoryCache loadFromDiskToMemoryCache after size : ${memoryCacheMap.size}, ${memoryCacheSize + addedSize} < $memoryCacheSizeMax"
        )

        memoryCacheKeyList.remove(key)
        memoryCacheKeyList.add(0, key)


        Log.d(
            "dhlog",
            "MemoryCache loadFromDiskToMemoryCache memoryCacheSize size added $addedSize, total : $memoryCacheSize"
        )
    }

    @Synchronized
    override fun removeLastCache() {
        val targetKey = memoryCacheKeyList.lastOrNull() ?: return
        if (memoryCacheMap[targetKey] != null) {
            if (memoryCacheMap[targetKey]!!.value is CacheState.Success) {
                val target = (memoryCacheMap[targetKey]!!.value as CacheState.Success).data
                val targetSize = target.allocationByteCount
                memoryCacheMap.remove(targetKey)
                memoryCacheSize -= targetSize
            }

            Log.d(
                "dhlog",
                "MemoryCache removeLastMemoryCache() memoryCacheSize size removed : ${memoryCacheMap[targetKey]}"
            )
        }

        memoryCacheKeyList.remove(targetKey)
    }

    suspend fun update(key: String, cacheState: CacheState) {
        if (memoryCacheMap[key] == null) {
            memoryCacheMap[key] = MutableStateFlow(cacheState)
        } else {
            memoryCacheMap[key]!!.emit(cacheState)
        }
    }

    fun cacheFlow(key: String): MutableStateFlow<CacheState> {
        var flow = memoryCacheMap[key]
        if (flow == null) {
            memoryCacheMap[key] = MutableStateFlow(CacheState.Waiting)
            flow = memoryCacheMap[key]
        }

        return flow!!
    }
}