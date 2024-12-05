package com.dh.galleryapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dh.galleryapp.core.database.data.ImageRemoteKey
import com.dh.galleryapp.core.database.data.ImageResponse


@Database(
    entities = [ImageResponse::class, ImageRemoteKey::class],
    version = 1,
    exportSchema = false
)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDatabase? = null

        fun getInstance(context: Context): ImageDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context, ImageDatabase::class.java, "database.db")
            .setJournalMode(JournalMode.TRUNCATE)
            .build()

    }
}