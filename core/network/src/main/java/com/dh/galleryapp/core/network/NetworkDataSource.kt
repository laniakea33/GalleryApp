package com.dh.galleryapp.core.network

import com.dh.galleryapp.core.network.data.ImageResponse
import java.io.InputStream

interface NetworkDataSource {

    suspend fun loadImageList(page: Int, limit: Int): List<ImageResponse>
    suspend fun downloadImage(url: String, filePath: String): InputStream
}