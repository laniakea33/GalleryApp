package com.dh.galleryapp.feature.list.keystatusmap

import com.dh.galleryapp.feature.model.ImageResult

class KeyResultNotifier {

    //  이미지 key별 index. 같은 key를 여러 index에서 요청할 경우를 위함.
    //  observe할 때 add되고 제거되지 않는다.
    //  observe할 때 isActive = true, dispose할 때 isActive = false
    private val keyIndexMap = KeyIndexMap()

    //  이미지 key별 응답 상태. 같은 key를 여러 index에서 요청할 경우를 위함.
    //  Wait, Loading, Success, Failure 4개만 가진다.
    //  해당 key가 처음 observe될 때 add, key를 observing하는 index가 모두 dispose되면 remove
    private val keyResultMap = mutableMapOf<String, ImageResult>()

    fun observe(key: String, index: Int) {
        keyIndexMap.putOrUpdate(key, index, true)

        if (keyResultMap[key] == null) {
            keyResultMap[key] = ImageResult.Waiting
        }
    }

    fun getImageResult(key: String): ImageResult? {
        return keyResultMap[key]
    }

    fun dispose(key: String, index: Int) {
        keyIndexMap.putOrUpdate(key, index, false)
        if (hasNoObserver(key)) {
            keyResultMap.remove(key)
        }
    }

    fun hasNoObserver(key: String): Boolean {
        return keyIndexMap.getActiveCount(key) == 0
    }

    fun updateImageResult(
        key: String,
        imageResult: ImageResult,
        notifier: (observerIndex: Int) -> Unit,
    ) {
        if (keyResultMap[key] != null) {
            keyResultMap[key] = imageResult
        }

        keyIndexMap.forEachActive(key, notifier)
    }
}