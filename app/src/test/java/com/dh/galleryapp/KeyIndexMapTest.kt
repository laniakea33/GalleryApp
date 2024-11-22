package com.dh.galleryapp

import com.dh.galleryapp.feature.list.model.KeyIndexMap
import com.dh.galleryapp.feature.list.model.UiIndex
import org.junit.Test

class KeyIndexMapTest {

    @Test
    fun UiIndex_equals_테스트() {
        assert(UiIndex(0, true) == UiIndex(0, true))
    }

    @Test
    fun UiIndex_equals_테스트_2() {
        assert(UiIndex(0, true) == UiIndex(0, false))
    }

    @Test
    fun UiIndex_equals_테스트_3() {
        assert(UiIndex(0, true) != UiIndex(1, true))
    }

    @Test
    fun UiIndex_equals_테스트_4() {
        assert(UiIndex(0, true) != UiIndex(1, false))
    }

    @Test
    fun KeyIndexMap_추가_테스트() {
        val map = KeyIndexMap()

        val key = "key"

        assert(map.putOrUpdate(key, 0, true))
        assert(map.contains(key, 0, true))
        assert(map.get(key, 0)!!.isActive)
    }

    @Test
    fun KeyIndexMap_포함_테스트() {
        val map = KeyIndexMap()

        val key = "key"

        assert(map.putOrUpdate(key, 0, true))
        assert(map.contains(key, 0, true))
        assert(map.contains(key, 0, false))
        assert(map.get(key, 0)!!.isActive)
    }

    @Test
    fun KeyIndexMap_추가_테스트_2() {
        val map = KeyIndexMap()

        val key = "key"

        assert(map.putOrUpdate(key, 0, true))
        assert(map.contains(key, 0, true))
        assert(map.get(key, 0)!!.isActive)
        assert(map.putOrUpdate(key, 0, false))
        assert(!map.get(key, 0)!!.isActive)
    }

    @Test
    fun KeyIndexMap_getActiveCount_테스트() {
        val map = KeyIndexMap()

        val key = "key"

        map.putOrUpdate(key, 0, true)
        map.putOrUpdate(key, 1, false)
        map.putOrUpdate(key, 2, true)
        map.putOrUpdate(key, 3, true)
        map.putOrUpdate(key, 3, false)
        map.putOrUpdate(key, 4, true)
        assert(map.getActiveCount(key) == 3)
    }

    @Test
    fun KeyIndexMap_forEach_테스트() {
        val map = KeyIndexMap()

        val key = "key"

        map.putOrUpdate(key, 0, true)
        map.putOrUpdate(key, 1, false)
        map.putOrUpdate(key, 2, true)
        map.putOrUpdate(key, 3, true)
        map.putOrUpdate(key, 3, false)
        map.putOrUpdate(key, 4, true)

        val list = mutableListOf<Int>()

        map.forEachActive(key) {
            list.add(it)
        }

        assert(list.size == 3)
    }
}