package com.dh.galleryapp.core.bitmapcache

import android.graphics.Bitmap
import com.dh.galleryapp.core.cache.memory.MemoryCacheObject

class BitmapCacheObject(private val bitmap: Bitmap) : MemoryCacheObject(bitmap) {
    override fun size(): Int {
        return bitmap.allocationByteCount
    }
}