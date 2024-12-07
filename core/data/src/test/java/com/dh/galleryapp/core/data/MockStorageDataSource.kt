package com.dh.galleryapp.core.data

import com.dh.galleryapp.core.storage.StorageDataSource
import java.io.File
import java.io.FileOutputStream

class MockStorageDataSource : StorageDataSource {

    companion object {
        const val mockFileLength = 1024 * 1024 * 200L
    }

    var textFiles = HashMap<String, String>()
    var fileSystem = HashMap<String, File>()

    override fun createFile(filePath: String): File {
        val file = File(filePath)
        fileSystem[filePath] = file
        return file
    }

    override fun readLines(filePath: String): List<String> {
        return textFiles[filePath]?.lines() ?: emptyList()
    }

    override fun fileNames(dirPath: String): List<String> {
        return fileSystem.keys.toList()
    }

    override fun deleteFile(filePath: String): Boolean {
        return fileSystem.remove(filePath) != null
    }

    override fun deleteFiles(filePathList: List<String>) {
        filePathList.forEach {
            deleteFile(it)
        }
    }

    override fun prependStringToFile(filePath: String, s: String) {
        if (textFiles[filePath] == null) textFiles[filePath] = ""

        textFiles[filePath]?.let {
            textFiles[filePath] = "$s\n$it"
        }
    }

    override fun removeStringFromFile(filePath: String, s: String) {
        textFiles[filePath]?.let {
            it.lines().filter { line ->
                !line.contains(s)
            }.also {
                textFiles[filePath] = it.joinToString("\n")
            }
        }
    }

    override fun fileExists(filePath: String): Boolean {
        return fileSystem.containsKey(filePath)
    }

    override fun fileLength(filePath: String): Long {
        return mockFileLength
    }

    override fun fileSizeSum(dirPath: String): Long {
        return fileSystem.size * fileLength("")
    }

    override fun writeFileOutputStreamToFile(
        dirPath: String,
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit,
    ) {
        TODO()
    }
}