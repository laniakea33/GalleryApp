package com.dh.galleryapp.core.network.retrofit

import com.dh.galleryapp.core.network.data.ImageResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ImageApi {

    @GET("/v2/list")
    suspend fun loadImageList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<ImageResponse>

    @Streaming
    @GET
    suspend fun loadImage(
        @Url url: String
    ): ResponseBody
}

