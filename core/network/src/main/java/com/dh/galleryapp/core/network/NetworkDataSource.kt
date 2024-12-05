package com.dh.galleryapp.core.network

import com.dh.galleryapp.core.network.data.ImageResponse
import okhttp3.ResponseBody

interface NetworkDataSource {

    suspend fun loadImageList(page: Int, limit: Int): List<ImageResponse>
    suspend fun downloadImage(url: String): ResponseBody
}