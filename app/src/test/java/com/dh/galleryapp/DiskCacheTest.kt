package com.dh.galleryapp

import com.dh.galleryapp.core.cache.disk.DiskCache
import com.dh.galleryapp.core.data.repository.mock.MockRepository
import com.dh.galleryapp.core.data.repository.mock.MockRepository.Companion.mockFileLength
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
        val diskCache = DiskCache(MockRepository(), "diskCacheDir", "journalFileDir")

        coroutineScope {
            for (i in 0 until 100) {
                launch(Dispatchers.Default) {
                    val key = i.toString()
                    val size = mockFileLength
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
    fun MockRepositoryTest(): Unit = runBlocking {
        val repository = MockRepository()
        repository.prependStringToFile("journal", "aaa")
        repository.prependStringToFile("journal", "bbb")
        repository.prependStringToFile("journal", "ccc")
        repository.readLines("journal").forEach {
            println(it)
        }

        repository.removeStringFromFile("journal", "bbb")

        repository.readLines("journal").forEach {
            println(it)
        }

    }
}