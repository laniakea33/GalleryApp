package com.dh.galleryapp.core.network

import com.dh.galleryapp.core.network.data.ImageResponse
import com.dh.galleryapp.core.network.retrofit.ImageApi
import okhttp3.ResponseBody
import javax.inject.Inject

class NetworkDataSourceImpl @Inject constructor(
    private val imageApi: ImageApi,
) : NetworkDataSource {

    override suspend fun loadImageList(page: Int, limit: Int): List<ImageResponse> {
        return imageApi.loadImageList(page, limit)
    }

    override suspend fun downloadImage(url: String): ResponseBody {
        return imageApi.loadImage(url)
    }
}