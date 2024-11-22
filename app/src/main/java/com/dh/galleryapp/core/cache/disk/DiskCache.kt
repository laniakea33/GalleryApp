package com.dh.galleryapp.core.cache.disk

import com.dh.galleryapp.core.cache.Cache
import com.dh.galleryapp.core.data.di.Real
import com.dh.galleryapp.core.data.repository.Repository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.annotations.TestOnly
import java.io.FileOutputStream
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DiskCache @Inject constructor(
    @Real private val repository: Repository,
    @Named("diskCacheDir") val diskCacheDir: String,
    @Named("journalFileDir") val journalFileDir: String,
) : Cache {

    companion object {
        const val diskCacheSizeBytesMax: Long = 1024 * 1024 * 700 // 700 MB
    }

    private val diskCacheKeyList = LinkedList<String>()
    private var diskCacheSize: Long = 0


    private val journalFileName = "journal.txt"
    private val journalFilePath = "$journalFileDir/$journalFileName"

    val mutex = Mutex()

    init {
        diskCacheSize = repository.fileSizeSum(diskCacheDir)
        loadJournalFile(diskCacheKeyList)
    }

    private fun loadJournalFile(into: MutableList<String>) {
        repository.createFile(journalFilePath)
        into.addAll(repository.readLines(journalFilePath))

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
    }

    override suspend fun isCached(key: String): Boolean {
        return mutex.withLock {
            val filePath = "$diskCacheDir/$key"
            repository.fileExists(filePath)
        }
    }

    override suspend fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        mutex.withLock {
            if (isNewData) {
                diskCacheSize += addedSize

                while (diskCacheSize >= diskCacheSizeBytesMax) {
                    removeLastCache()
                }
            }

            if (diskCacheKeyList.contains(key)) {
                diskCacheKeyList.remove(key)
            }

            repository.removeStringFromFile(journalFilePath, key)

            if (repository.fileExists("$diskCacheDir/$key")) {
                diskCacheKeyList.add(0, key)
                repository.prependStringToFile(journalFilePath, key)
            }
        }
    }

    override fun removeLastCache() {
        println("와일문")
        val targetKey = diskCacheKeyList.lastOrNull() ?: return
        val targetFilePath = "$diskCacheDir/$targetKey"


        if (repository.fileExists(targetFilePath)) {
            val targetSize = repository.fileLength(targetFilePath)
            repository.deleteFile(targetFilePath)
            diskCacheSize -= targetSize
        }

        diskCacheKeyList.remove(targetKey)

        repository.removeStringFromFile(journalFilePath, targetKey)
    }

    suspend fun saveFileOutputStreamToDiskCache(
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit,
    ) {
        mutex.withLock {
            repository.writeFileOutputStreamToFile(
                diskCacheDir,
                fileName,
                onFileOutputStream = onFileOutputStream
            )
        }
    }

    @TestOnly
    fun newCache(key: String) {
        if (!diskCacheKeyList.contains(key)) {
            diskCacheKeyList.add(0, key)
            repository.prependStringToFile(journalFilePath, key)
            repository.createFile("$diskCacheDir/$key")
        }
    }

    @TestOnly
    fun getDiskCacheFileCount(): Int {
        return repository.fileNames(diskCacheDir).size
    }

    fun getDiskCacheKeyList(): List<String> {
        return diskCacheKeyList
    }
}
