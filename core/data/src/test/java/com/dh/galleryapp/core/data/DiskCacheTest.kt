package com.dh.galleryapp.core.data

import com.dh.galleryapp.core.cache.disk.DiskCache
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
class DiskCacheTest {

    @Test
    fun 캐시추가_LRU_테스트() = runBlocking {
        val diskCache = DiskCache(MockStorageDataSource(), "diskCacheDir", "journalFileDir")

        coroutineScope {
            for (i in 0 until 100) {
                launch(Dispatchers.Default) {
                    val key = i.toString()
                    val size = MockStorageDataSource.mockFileLength
                    diskCache.newCache(key)
                    diskCache.lruCacheProcess(key, true, size)
                }
            }
        }

        assertEquals(
            diskCache.getDiskCacheFileCount(),
            diskCache.getDiskCacheKeyList().size
        )
    }

    @Test
    fun MockImageRepositoryTest(): Unit = runBlocking {
        val mockDataSource = MockStorageDataSource()
        mockDataSource.prependStringToFile("journal", "aaa")
        mockDataSource.prependStringToFile("journal", "bbb")
        mockDataSource.prependStringToFile("journal", "ccc")
        mockDataSource.readLines("journal").forEach {
            println(it)
        }

        mockDataSource.removeStringFromFile("journal", "bbb")

        mockDataSource.readLines("journal").forEach {
            println(it)
        }

    }
}