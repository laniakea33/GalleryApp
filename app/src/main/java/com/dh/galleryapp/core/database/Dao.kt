package com.dh.galleryapp.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dh.galleryapp.core.database.data.ImageRemoteKey
import com.dh.galleryapp.core.database.data.ImageResponse

@Dao
interface ImageDao {
    @Query("SELECT * FROM images")
    fun pagingSource(): PagingSource<Int, ImageResponse>

    @Query("SELECT * FROM images ORDER BY id")
    fun getImages(): List<ImageResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveImages(images: List<ImageResponse>)

    @Query("DELETE FROM images")
    suspend fun clearImages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRemoteKey(keys: ImageRemoteKey)

    @Query("SELECT * FROM image_remote_keys WHERE id=:id")
    suspend fun getRemoteKeyByImageId(id: Int): ImageRemoteKey?

    @Query("DELETE FROM image_remote_keys")
    suspend fun clearRemoteKeys()

    @Query("SELECT * FROM image_remote_keys WHERE id = (SELECT MAX(id) FROM image_remote_keys)")
    suspend fun getLastRemoteKey(): ImageRemoteKey?
}