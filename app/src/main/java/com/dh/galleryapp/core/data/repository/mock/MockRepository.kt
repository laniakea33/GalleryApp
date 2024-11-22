package com.dh.galleryapp.core.data.repository.mock

import androidx.paging.PagingData
import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.model.Image
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class MockRepository @Inject constructor() : Repository {

    companion object {
        const val mockFileLength = 1024 * 1024 * 200L
    }

    private val dummyImages = buildList {
        for (i in 0 until 10) {
            Image(
                id = i.toString(),
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3000,
                url = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/0/200/300",
            ).also {
                add(it)
            }
        }

        for (i in 0 until 10) {
            Image(
                id = i.toString(),
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3000,
                url = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/10/200/300",
            ).also {
                add(it)
            }
        }
    }

    var textFiles = HashMap<String, String>()
    var fileSystem = HashMap<String, File>()

    override fun loadImageList(): Flow<PagingData<Image>> {
        return MutableStateFlow(PagingData.from(dummyImages))
    }

    override suspend fun downloadImage(url: String, filePath: String): Result<String> {
        return Result.success(filePath)
    }

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

    }
}