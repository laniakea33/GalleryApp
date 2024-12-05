package com.dh.galleryapp.core.storage

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class StorageDataSourceImpl @Inject constructor(
) : StorageDataSource {
    override fun createFile(filePath: String): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    override fun readLines(filePath: String): List<String> {
        val file = File(filePath)
        return file.readLines()
    }

    override fun fileNames(dirPath: String): List<String> {
        return File(dirPath)
            .listFiles()
            ?.filter { it.isFile }
            ?.map {
                it.name
            } ?: listOf()
    }

    override fun deleteFile(filePath: String): Boolean {
        return File(filePath).delete()
    }

    override fun deleteFiles(filePathList: List<String>) {
        filePathList.forEach {
            File(it).delete()
        }
    }

    override fun prependStringToFile(filePath: String, s: String) {
        val file = File(filePath)
        val lines = file.readLines()
        val bufferedWrite = file.bufferedWriter()

        if (lines.isNotEmpty()) {
            bufferedWrite.write(s + "\n")
            lines.forEachIndexed { i, item ->
                if (i < lines.size - 1) {
                    bufferedWrite.write(item + "\n")
                } else {
                    bufferedWrite.write(item)
                }
            }
        } else {
            bufferedWrite.write(s)
        }

        bufferedWrite.flush()
        bufferedWrite.close()
    }

    override fun removeStringFromFile(filePath: String, s: String) {
        val file = File(filePath)
        val lines = file.readLines()    //  개행문자를 제외하고 List<String>을 반환

        val modifiedLine = lines.mapIndexed { index, st ->
            if (st.contains(s)) {
                st.replace(s, "")
            } else st
        }

        val bufferedWrite = file.bufferedWriter()
        modifiedLine.forEachIndexed { index, st ->
            if (st.isNotBlank()) {
                if (index < modifiedLine.size - 1) {
                    bufferedWrite.write(st + "\n")
                } else {
                    bufferedWrite.write(st)
                }
            }
        }
        bufferedWrite.flush()
        bufferedWrite.close()
    }

    override fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    override fun fileLength(filePath: String): Long {
        return File(filePath).length()
    }

    override fun fileSizeSum(dirPath: String): Long {
        var sizeSum = 0L
        File(dirPath)
            .listFiles()
            ?.filter { it.isFile }
            ?.let { list ->
                list.forEach { sizeSum += it.length() }
            }
        return sizeSum
    }

    override fun writeFileOutputStreamToFile(
        dirPath: String,
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit
    ) {

        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File("$dirPath/$fileName")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            onFileOutputStream(out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

