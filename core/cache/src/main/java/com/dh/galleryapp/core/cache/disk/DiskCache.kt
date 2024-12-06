package com.dh.galleryapp.core.cache.disk

import com.dh.galleryapp.core.cache.Cache
import com.dh.galleryapp.core.storage.StorageDataSource
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
    private val storageDataSource: StorageDataSource,
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
        diskCacheSize = storageDataSource.fileSizeSum(diskCacheDir)
        loadJournalFile(diskCacheKeyList)
    }

    private fun loadJournalFile(into: MutableList<String>) {
        storageDataSource.createFile(journalFilePath)
        into.addAll(storageDataSource.readLines(journalFilePath))

        val fileNames = storageDataSource.fileNames(diskCacheDir)

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

        storageDataSource.deleteFiles(fakeFileNames)
    }

    override suspend fun isCached(key: String): Boolean {
        return mutex.withLock {
            val filePath = "$diskCacheDir/$key"
            storageDataSource.fileExists(filePath)
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

            storageDataSource.removeStringFromFile(journalFilePath, key)

            if (storageDataSource.fileExists("$diskCacheDir/$key")) {
                diskCacheKeyList.add(0, key)
                storageDataSource.prependStringToFile(journalFilePath, key)
            }
        }
    }

    override fun removeLastCache() {
        println("와일문")
        val targetKey = diskCacheKeyList.lastOrNull() ?: return
        val targetFilePath = "$diskCacheDir/$targetKey"


        if (storageDataSource.fileExists(targetFilePath)) {
            val targetSize = storageDataSource.fileLength(targetFilePath)
            storageDataSource.deleteFile(targetFilePath)
            diskCacheSize -= targetSize
        }

        diskCacheKeyList.remove(targetKey)

        storageDataSource.removeStringFromFile(journalFilePath, targetKey)
    }

    suspend fun saveFileOutputStreamToDiskCache(
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit,
    ) {
        mutex.withLock {
            storageDataSource.writeFileOutputStreamToFile(
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
            storageDataSource.prependStringToFile(journalFilePath, key)
            storageDataSource.createFile("$diskCacheDir/$key")
        }
    }

    @TestOnly
    fun getDiskCacheFileCount(): Int {
        return storageDataSource.fileNames(diskCacheDir).size
    }

    fun getDiskCacheKeyList(): List<String> {
        return diskCacheKeyList
    }
}
