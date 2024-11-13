package com.dh.galleryapp.core.storage

import java.io.File
import java.io.FileOutputStream

interface StorageDataSource {
    fun createFile(filePath: String): File
    fun readLines(filePath: String): List<String>
    fun fileNames(dirPath: String): List<String>
    fun deleteFile(filePath: String): Boolean
    fun deleteFiles(filePathList: List<String>)
    fun prependStringToFile(filePath: String, s: String)
    fun removeStringFromFile(filePath: String, s: String)
    fun fileExists(filePath: String): Boolean
    fun fileLength(filePath: String): Long
    fun fileSizeSum(dirPath: String): Long
    fun writeFileOutputStreamToFile(
        dirPath: String,
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit
    )

}