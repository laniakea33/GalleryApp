package com.dh.galleryapp.core.cache

import android.graphics.Bitmap
import android.util.Log
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor() : Cache {

    private val memoryCacheMap = HashMap<String, Bitmap>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0
    private val memoryCacheSizeMax: Long = 1024 * 1024 * 20 // 20mb

    @Synchronized
    override fun isCached(key: String): Boolean {
        return memoryCacheMap[key] != null
    }

    @Synchronized
    fun cachedImage(key: String): Bitmap? {
        return memoryCacheMap[key]
    }

    @Synchronized
    override fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        Log.d("dhlog", "MemoryCache lruCacheProcess() : $addedSize / ${memoryCacheSizeMax}")
        if (isNewData) {
            memoryCacheSize += addedSize

            while (memoryCacheSize >= memoryCacheSizeMax) {
                Log.d(
                    "dhlog",
                    "MemoryCache removeLastCache() : $memoryCacheSize / ${memoryCacheSizeMax}"
                )
                removeLastCache()
            }
        }

        memoryCacheKeyList.remove(key)
        memoryCacheKeyList.add(0, key)
    }

    @Synchronized
    override fun removeLastCache() {
        val targetKey = memoryCacheKeyList.lastOrNull() ?: return

        memoryCacheMap[targetKey]?.let {
            val targetSize = it.allocationByteCount
            memoryCacheSize -= targetSize
        }

        memoryCacheMap.remove(targetKey)
        memoryCacheKeyList.remove(targetKey)
    }

    @Synchronized
    fun newCache(key: String, bitmap: Bitmap) {
        Log.d(
            "dhlog",
            "MemoryCache newCache : $key, 이미 있는 키? : ${memoryCacheKeyList.contains(key)}"
        )
        if (!memoryCacheKeyList.contains(key)) {
            memoryCacheKeyList.add(0, key)
            memoryCacheMap[key] = bitmap
        }
    }
}