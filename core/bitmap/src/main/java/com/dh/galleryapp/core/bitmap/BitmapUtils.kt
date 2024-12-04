package com.dh.galleryapp.core.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {

    fun decodeSample(filePath: String, width: Int, height: Int): Bitmap? {
        return BitmapFactory.Options().run {
            //  Bitmap을 실제로 로드하지 않고 정보만 가져올 수 있다.
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(filePath, this)

            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(filePath, this)

            return@run bitmap
        }
    }

    fun decode(filePath: String): Bitmap? {
        return BitmapFactory.Options().let {
            BitmapFactory.decodeFile(filePath, it)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val (width: Int, height: Int) = options.run { outWidth to outHeight }
        var inSampleSize = 1

        while (width / inSampleSize > reqWidth || height / inSampleSize > reqHeight) {
            inSampleSize *= 2
        }

        return inSampleSize
    }

}