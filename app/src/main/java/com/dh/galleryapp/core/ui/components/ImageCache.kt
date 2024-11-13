package com.dh.galleryapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dh.galleryapp.core.imagecache.ImageCache
import com.dh.galleryapp.core.imagecache.di.ImageCacheEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun rememberImageCache(): ImageCache {
    val context = LocalContext.current
    return remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImageCacheEntryPoint::class.java
        )
        entryPoint.imageCache()
    }
}