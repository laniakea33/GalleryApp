package com.dh.galleryapp.feature.list.keystatusmap

import com.dh.galleryapp.feature.list.model.ImageResult

class KeyResultNotifier  {

    //  이미지 key별 index. 같은 key를 여러 index에서 요청할 경우를 위함.
    //  observe할 때 add되고 제거되지 않는다.
    //  observe할 때 isActive = true, dispose할 때 isActive = false
    private val observers = KeyIndexMap()

    //  이미지 key별 응답 상태. 같은 key를 여러 index에서 요청할 경우를 위함.
    //  Wait, Loading, Success, Failure 4개만 가진다.
    //  해당 key가 처음 observe될 때 add, key를 observing하는 index가 모두 dispose되면 remove
    private val imageResults = mutableMapOf<String, ImageResult>()

    fun observe(key: String, index: Int) {
        observers.putOrUpdate(key, index, true)

        if (imageResults[key] == null) {
            imageResults[key] = ImageResult.Waiting
        }
    }

    fun getImageResult(key: String): ImageResult? {
        return imageResults[key]
    }

    fun dispose(key: String, index: Int) {
        observers.putOrUpdate(key, index, false)
        if (hasNoObserver(key)) {
            imageResults.remove(key)
        }
    }

    fun hasNoObserver(key: String): Boolean {
        return observers.getActiveCount(key) == 0
    }

    fun updateImageResult(key: String, imageResult: ImageResult, notifier: (observerIndex: Int) -> Unit) {
        if (imageResults[key] != null) {
            imageResults[key] = imageResult
        }

        observers.forEachActive(key, notifier)
    }
}