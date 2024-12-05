package com.dh.galleryapp.core.cache.memory

import com.dh.galleryapp.core.cache.Cache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.annotations.TestOnly
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor() : Cache {

    companion object {
        const val memoryCacheSizeMax: Long = 1024 * 1024 * 40 // 40mb
    }

    private val memoryCacheMap = HashMap<String, MemoryCacheObject>()
    private val memoryCacheKeyList = LinkedList<String>()
    private var memoryCacheSize: Long = 0

    val mutex = Mutex()

    override suspend fun isCached(key: String): Boolean {
        return mutex.withLock {
            memoryCacheMap[key] != null
        }
    }

    suspend fun cachedImage(key: String): MemoryCacheObject? {
        return mutex.withLock { memoryCacheMap[key] }
    }

    override suspend fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        mutex.withLock {
            if (isNewData) {
                memoryCacheSize += addedSize

                while (memoryCacheSize >= memoryCacheSizeMax) {
                    removeLastCache()
                }
            }

            memoryCacheKeyList.remove(key)
            if (memoryCacheMap.containsKey(key)) {
                memoryCacheKeyList.add(0, key)
            }
        }

    }

    override fun removeLastCache() {
        val targetKey = memoryCacheKeyList.lastOrNull() ?: return

        memoryCacheMap[targetKey]?.let {
            val targetSize = it.size()
            memoryCacheSize -= targetSize
        }

        memoryCacheMap.remove(targetKey)
        memoryCacheKeyList.remove(targetKey)
    }

    suspend fun newCache(key: String, t: MemoryCacheObject) {
        mutex.withLock {
            if (!memoryCacheKeyList.contains(key)) {
                memoryCacheKeyList.add(0, key)
                memoryCacheMap[key] = t
            }
        }
    }

    @TestOnly
    fun getMemoryCacheMap(): HashMap<String, MemoryCacheObject> {
        return memoryCacheMap
    }

    @TestOnly
    fun getMemoryCacheKeyList(): LinkedList<String> {
        return memoryCacheKeyList
    }
}

