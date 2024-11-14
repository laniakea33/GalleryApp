package com.dh.galleryapp.core.cache

import android.content.Context
import com.dh.galleryapp.core.data.repository.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiskCache @Inject constructor(
    private val repository: Repository,
    @ApplicationContext context: Context,
) : Cache {

    private val diskCacheKeyList = LinkedList<String>()
    private var diskCacheSize: Long = 0
    private val diskCacheSizeBytesMax: Long = 1024 * 1024 * 700 // 700 MB

    val diskCacheDir: String = context.externalCacheDir!!.absolutePath
    private val journalFileDir = context.filesDir
    private val journalFileName = "journal.txt"
    private val journalFilePath = "$journalFileDir/$journalFileName"

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

    @Synchronized
    override fun isCached(key: String): Boolean {
        val filePath = "$diskCacheDir/$key"
        return repository.fileExists(filePath)
    }

    @Synchronized
    override fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
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

        diskCacheKeyList.add(0, key)
        repository.prependStringToFile(journalFilePath, key)
    }

    @Synchronized
    override fun removeLastCache() {
        val targetKey = diskCacheKeyList.lastOrNull() ?: return
        val targetFileName = "$diskCacheDir/$targetKey"


        if (repository.fileExists(targetFileName)) {
            val targetSize = repository.fileLength(targetFileName)
            repository.deleteFile(targetFileName)
            diskCacheSize -= targetSize
        }

        diskCacheKeyList.remove(targetKey)

        repository.removeStringFromFile(journalFilePath, targetKey)
    }

    @Synchronized
    fun saveFileOutputStreamToDiskCache(
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit
    ) {
        repository.writeFileOutputStreamToFile(
            diskCacheDir,
            fileName,
            onFileOutputStream = onFileOutputStream
        )
    }
}