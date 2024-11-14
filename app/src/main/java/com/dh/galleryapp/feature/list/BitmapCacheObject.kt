package com.dh.galleryapp.feature.list

import android.graphics.Bitmap
import com.dh.galleryapp.core.cache.memory.MemoryCacheObject

class BitmapCacheObject(val bitmap: Bitmap) : MemoryCacheObject(bitmap) {
    override fun size(): Int {
        return bitmap.allocationByteCount
    }
}