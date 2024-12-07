package com.dh.galleryapp.core.data

import android.graphics.BitmapFactory
import com.dh.galleryapp.core.cache.memory.MemoryCache
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MemoryCacheTest {

    @Test
    fun 캐시추가_LRU_테스트_캐시사이즈보다_큰_객체() = runBlocking {
        val memoryCache = MemoryCache()

        coroutineScope {
            for (i in 0 until 10000) {
                launch(Dispatchers.Default) {
                    val key = i.toString()
                    val size = MemoryCache.memoryCacheSizeMax * 2
                    memoryCache.newCache(key, BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0))
                    memoryCache.lruCacheProcess(key, true, size)
                }
            }
        }

        assertEquals(memoryCache.getMemoryCacheMap().size, memoryCache.getMemoryCacheKeyList().size)
    }

    @Test
    fun 캐시추가_LRU_테스트_캐시사이즈보다_작은_객체() = runBlocking {
        val memoryCache = MemoryCache()

        coroutineScope {
            for (i in 0 until 10000) {
                launch(Dispatchers.Default) {
                    val key = i.toString()
                    val size = MemoryCache.memoryCacheSizeMax / 10 * 3
                    memoryCache.newCache(key, BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0))
                    memoryCache.lruCacheProcess(key, true, size)
                }
            }
        }

        assertEquals(memoryCache.getMemoryCacheMap().size, memoryCache.getMemoryCacheKeyList().size)
    }
}
