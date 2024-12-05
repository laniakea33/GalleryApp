package com.dh.galleryapp.core.network

import com.dh.galleryapp.core.network.data.ImageResponse
import com.dh.galleryapp.core.network.retrofit.ImageApi
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class NetworkDataSourceImpl @Inject constructor(
    private val imageApi: ImageApi,
) : NetworkDataSource {

    override suspend fun loadImageList(page: Int, limit: Int): List<ImageResponse> {
        return imageApi.loadImageList(page, limit)
    }

    override suspend fun downloadImage(url: String, filePath: String): InputStream {
        return imageApi.loadImage(url).byteStream()
    }
}