package com.dh.galleryapp.core.cache.memory

abstract class MemoryCacheObject(
    val data: Any,
) : HasSize

interface HasSize {
    fun size(): Int
}