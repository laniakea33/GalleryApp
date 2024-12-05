package com.dh.galleryapp.feature.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
sealed class ImageResult {
    data class Success(val data: Bitmap) : ImageResult()
    data class Failure(val t: Throwable) : ImageResult()
    data object Loading : ImageResult()
    //  해당 key의 이미지가 로딩이 필요한 상태
    data object Waiting : ImageResult()
    //  해당 key의 이미지의 상태를 모름
    data object Unknown : ImageResult()
}