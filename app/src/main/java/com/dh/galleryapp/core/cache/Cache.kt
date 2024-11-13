package com.dh.galleryapp.core.cache

interface Cache {
    fun isCached(key: String): Boolean
    fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0)
    fun removeLastCache()
}