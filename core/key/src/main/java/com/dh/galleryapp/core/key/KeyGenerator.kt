package com.dh.galleryapp.core.key

import java.net.URLEncoder

//  캐싱에 사용될 key이자 캐시 파일의 이름
object KeyGenerator {

    fun key(url: String): String {
        return "${URLEncoder.encode(url)}.jpg"
    }

    fun key(url: String, width: Int, height: Int): String {
        return "${URLEncoder.encode(url)}_${width}_$height.jpg"
    }
}