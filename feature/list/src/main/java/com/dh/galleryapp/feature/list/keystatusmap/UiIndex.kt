package com.dh.galleryapp.feature.list.keystatusmap

import java.util.Objects

data class UiIndex(
    val index: Int,
    val isActive: Boolean,  //  이미지 로딩 상태 업데이트를 받는지 여부. Composable이 활성상태여야 한다.
) {
    override fun equals(other: Any?): Boolean {
        return (other as? UiIndex != null && other.index == index)
    }

    override fun hashCode(): Int {
        return Objects.hash(index)
    }
}
