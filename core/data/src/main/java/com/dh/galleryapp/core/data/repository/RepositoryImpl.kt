package com.dh.galleryapp.core.data.repository

import android.graphics.Bitmap
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dh.galleryapp.core.bitmap.BitmapUtils
import com.dh.galleryapp.core.cache.disk.DiskCache
import com.dh.galleryapp.core.cache.memory.MemoryCache
import com.dh.galleryapp.core.data.repository.mapper.toImage
import com.dh.galleryapp.core.data.repository.paging.ImageRemoteMediator
import com.dh.galleryapp.core.database.LocalDataSource
import com.dh.galleryapp.core.domain.OriginalImageResult
import com.dh.galleryapp.core.domain.ThumbnailImageResult
import com.dh.galleryapp.core.domain.repository.Repository
import com.dh.galleryapp.core.key.KeyGenerator
import com.dh.galleryapp.core.model.Image
import com.dh.galleryapp.core.network.NetworkDataSource
import com.dh.galleryapp.core.storage.StorageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache,
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    private val imageRemoteMediator: ImageRemoteMediator,
    private val storageDataSource: StorageDataSource,
) : Repository {
    @OptIn(ExperimentalPagingApi::class)
    override fun loadImageList(): Flow<PagingData<Image>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = true,
                initialLoadSize = pageSize * 3,
                prefetchDistance = pageSize / 2,
            ),
            pagingSourceFactory = { localDataSource.pagingSource() },
            remoteMediator = imageRemoteMediator,
        ).flow
            .map { pagingData ->
                pagingData.map { it.toImage() }
            }
    }

    override suspend fun getSampledImage(
        url: String,
        width: Int,
        height: Int,
    ): Flow<ThumbnailImageResult> {
        return flow {
            emit(ThumbnailImageResult.Loading)

            val originKey = KeyGenerator.key(url)
            val key = KeyGenerator.key(url, width, height)

            if (memoryCache.isCached(key)) {
                val bitmap = memoryCache.cachedItem(key)!!
                emit(ThumbnailImageResult.Success(bitmap))
                memoryCache.lruCacheProcess(key, false)
                diskCache.lruCacheProcess(key, false)
                return@flow
            }

            val orgFilePath = "${diskCache.diskCacheDir}/$originKey"
            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!
                emit(ThumbnailImageResult.Success(bitmap))

                memoryCache.newCache(key, bitmap)
                memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())
                diskCache.lruCacheProcess(key, false)

            } else if (diskCache.isCached(originKey)) {
                val bitmap = BitmapUtils.decodeSample(orgFilePath, width, height)!!
                emit(ThumbnailImageResult.Success(bitmap))

                yield()

                memoryCache.newCache(key, bitmap)
                memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())

                yield()

                diskCache.saveFileOutputStreamToDiskCache(
                    key,
                    onFileOutputStream = {
                        // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                )
                diskCache.lruCacheProcess(key, false)

            } else {
                val result = downloadImage(url, orgFilePath)

                if (result.isSuccess) {
                    try {
                        val orgFileName = result.getOrThrow()
                        val fileSize = fileLength(orgFileName)

                        diskCache.lruCacheProcess(originKey, true, fileSize)

                        yield()

                        val bitmap = BitmapUtils.decodeSample(orgFilePath, width, height)!!
                        emit(ThumbnailImageResult.Success(bitmap))

                        yield()

                        memoryCache.newCache(key, bitmap)
                        memoryCache.lruCacheProcess(key, true, bitmap.allocationByteCount.toLong())

                        yield()

                        diskCache.saveFileOutputStreamToDiskCache(
                            key,
                            onFileOutputStream = {
                                // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                            }
                        )

                        val sampledFileSize = File(filePath).length()

                        diskCache.lruCacheProcess(key, true, sampledFileSize)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        emit(ThumbnailImageResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        emit(ThumbnailImageResult.Failure(it))
                    }
                }
            }
        }
    }

    override fun getOriginalImage(thumbnailKey: String, url: String): Flow<OriginalImageResult> {
        return flow {
            emit(OriginalImageResult.Loading)

            val key = KeyGenerator.key(url)

            if (memoryCache.isCached(thumbnailKey)) {
                val bitmap = memoryCache.cachedItem(thumbnailKey)!!
                emit(OriginalImageResult.Success(bitmap))
            }

            val filePath = "${diskCache.diskCacheDir}/$key"

            yield()

            if (diskCache.isCached(key)) {
                val bitmap = BitmapUtils.decode(filePath)!!

                emit(OriginalImageResult.Success(bitmap))

                yield()

                diskCache.lruCacheProcess(key, false)

            } else {
                val result = downloadImage(url, filePath)

                if (result.isSuccess) {
                    try {
                        yield()

                        val bitmap = BitmapUtils.decode(filePath)!!

                        emit(OriginalImageResult.Success(bitmap))

                        val fileSize = fileLength(filePath)

                        diskCache.lruCacheProcess(key, true, fileSize)

                    } catch (e: Exception) {
                        emit(OriginalImageResult.Failure(e))
                    }

                } else {
                    result.exceptionOrNull().let {
                        it ?: RuntimeException("알수없는 오류 발생")
                    }.also {
                        emit(OriginalImageResult.Failure(it))
                    }
                }
            }
        }
    }

    override suspend fun downloadImage(url: String, filePath: String): Result<String> {
        return try {
            networkDataSource.downloadImage(url = url, filePath = filePath)
                .use { fileIn ->
                    val file = File(filePath)

                    storageDataSource.writeFileOutputStreamToFile(
                        dirPath = file.parent!!,
                        fileName = file.name,
                    ) { fileOut ->
                        fileIn.copyTo(fileOut)
                    }
                }
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createFile(filePath: String): File {
        return storageDataSource.createFile(filePath)
    }

    override fun readLines(filePath: String): List<String> {
        return storageDataSource.readLines(filePath)
    }

    override fun fileNames(dirPath: String): List<String> {
        return storageDataSource.fileNames(dirPath)
    }

    override fun deleteFile(filePath: String): Boolean {
        return storageDataSource.deleteFile(filePath)
    }

    override fun deleteFiles(filePathList: List<String>) {
        return storageDataSource.deleteFiles(filePathList)
    }

    override fun prependStringToFile(filePath: String, s: String) {
        return storageDataSource.prependStringToFile(filePath, s)
    }

    override fun removeStringFromFile(filePath: String, s: String) {
        return storageDataSource.removeStringFromFile(filePath, s)
    }

    override fun fileExists(filePath: String): Boolean {
        return storageDataSource.fileExists(filePath)
    }

    override fun fileLength(filePath: String): Long {
        return storageDataSource.fileLength(filePath)
    }

    override fun fileSizeSum(dirPath: String): Long {
        return storageDataSource.fileSizeSum(dirPath)
    }

    override fun writeFileOutputStreamToFile(
        dirPath: String,
        fileName: String,
        onFileOutputStream: (FileOutputStream) -> Unit,
    ) {
        return storageDataSource.writeFileOutputStreamToFile(dirPath, fileName, onFileOutputStream)
    }
}
