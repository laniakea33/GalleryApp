package com.dh.galleryapp.core.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtils {

    fun decodeSample(filePath: String, width: Int, height: Int): Bitmap? {
        return BitmapFactory.Options().run {
            //  Bitmap을 실제로 로드하지 않고 정보만 가져올 수 있다.
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(filePath, this)

            Log.d("dhlog", "decodeSample 샘플링 전 << $outWidth x $outHeight")

            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(filePath, this)

            Log.d("dhlog", "decodeSample 샘플링 전 << $outWidth x $outHeight")

            return@run bitmap
        }
    }

    fun decode(filePath: String): Bitmap? {
        return BitmapFactory.Options().let {
            BitmapFactory.decodeFile(filePath, it)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (width: Int, height: Int) = options.run { outWidth to outHeight }
        var inSampleSize = 1

        while (width / inSampleSize > reqWidth || height / inSampleSize > reqHeight) {
            inSampleSize *= 2
        }

        return inSampleSize
    }

    fun saveBitmapToFile(bitmap: Bitmap, dirPath: String, fileName: String) {
        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File("$dirPath/$fileName")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            // Bitmap을 압축하여 파일에 저장 (JPEG 포맷, 압축 품질 100)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}