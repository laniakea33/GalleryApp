package com.dh.galleryapp.core.cache

interface Cache {
    suspend fun isCached(key: String): Boolean
    suspend fun lruCacheProcess(key: String, isNewData: Boolean, addedSize: Long = 0)
    fun removeLastCache()
}