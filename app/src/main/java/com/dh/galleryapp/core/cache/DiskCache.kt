package com.dh.galleryapp.core.cache

import android.content.Context
import android.util.Log
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
    var diskCacheSize: Long = 0
    private val diskCacheSizeBytesMax: Long = 1024 * 1024 * 100 // 100 MB

    val diskCacheDir = context.externalCacheDir!!.absolutePath
    private val journalFileDir = context.filesDir
    private val journalFileName = "journal.txt"
    private val journalFilePath = "$journalFileDir/$journalFileName"

    init {
        diskCacheSize = repository.fileSizeSum(diskCacheDir)
        Log.d("dhlog", "DiskCache init() diskCacheSize $diskCacheSize")
        loadJournalFile(diskCacheKeyList)
    }

    private fun loadJournalFile(into: MutableList<String>) {
        repository.createFile(journalFilePath)
        into.addAll(repository.readLines(journalFilePath))

        Log.d("dhlog", "DiskCache loadJournalFile() ${into.size}")

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

        Log.d(
            "dhlog",
            "DiskCache loadJournalFile() fakeKey : ${fakeKey.size}, fakeFiles : ${fakeFileNames.size}"
        )
    }

    override fun isCached(key: String): Boolean {
        val filePath = "$diskCacheDir/$key"
        return repository.fileExists(filePath).also {
            if (it) Log.d("dhlog", "DiskCache isCachedInDisk() : $key")
        }
    }

    @Synchronized
    override fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long) {
        Log.d("dhlog", "DiskCache diskLruCacheProcess() : $key")
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

        Log.d(
            "dhlog",
            "DiskCache diskLruCacheProcess() : $key diskCacheKeyList : ${diskCacheKeyList.size} fin"
        )
    }

    override fun removeLastCache() {
        val targetKey = diskCacheKeyList.lastOrNull() ?: return
        val targetFileName = "$diskCacheDir/$targetKey"


        if (repository.fileExists(targetFileName)) {
            val targetSize = repository.fileLength(targetFileName)
            repository.deleteFile(targetFileName)
            diskCacheSize -= targetSize
            Log.d("dhlog", "DiskCache diskLruCacheProcess() 와일문도는중 : 실제로 파일이 삭제됨")
        }

        Log.d(
            "dhlog",
            "DiskCache diskLruCacheProcess() 와일문도는중 : $targetKey, diskCacheSize : $diskCacheSize > $diskCacheSizeBytesMax"
        )

        diskCacheKeyList.remove(targetKey)

        Log.d(
            "dhlog",
            "DiskCache diskLruCacheProcess() 와일문도는중 : 파일 삭제 됨 keySize :${diskCacheKeyList.size}, file size : ${
                repository.fileSizeSum(diskCacheDir)
            }"
        )

        repository.removeStringFromFile(journalFilePath, targetKey)
    }

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