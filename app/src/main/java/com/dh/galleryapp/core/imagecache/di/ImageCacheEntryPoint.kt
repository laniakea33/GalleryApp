package com.dh.galleryapp.core.imagecache.di

import com.dh.galleryapp.core.imagecache.ImageCache
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageCacheEntryPoint {
    fun imageCache(): ImageCache
}