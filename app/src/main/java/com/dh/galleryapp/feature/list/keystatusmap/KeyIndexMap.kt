package com.dh.galleryapp.feature.list.keystatusmap

import com.dh.galleryapp.feature.list.model.UiIndex

class KeyIndexMap {
    private val map = HashMap<String, HashSet<UiIndex>>()

    fun putOrUpdate(key: String, index: Int, isActive: Boolean): Boolean {
        val uiIndex = UiIndex(index, isActive)

        val set = map.getOrPut(key) { HashSet() }

        if (set.contains(uiIndex)) {
            set.remove(uiIndex)
        }

        return set.add(uiIndex)
    }

    fun contains(key: String, index: Int, isActive: Boolean): Boolean {
        return map[key]?.contains(UiIndex(index, isActive)) == true
    }

    fun size(key: String): Int {
        return map[key]?.size ?: 0
    }

    fun get(key: String, index: Int): UiIndex? {
        map[key]?.forEach {
            if (it.index == index) {
                return it
            }
        }

        return null
    }

    fun getActiveCount(key: String): Int {
        return map[key]?.count {
            it.isActive
        } ?: 0
    }


    fun forEachActive(key: String, action: (Int) -> Unit) {
        map[key]?.filter {
            it.isActive
        }?.map {
            it.index
        }?.forEach { i ->
            action(i)
        }
    }
}