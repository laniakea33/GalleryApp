package com.dh.galleryapp.core.storage.di

import com.dh.galleryapp.core.storage.StorageDataSource
import com.dh.galleryapp.core.storage.StorageDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {

    @Binds
    fun binds(impl: StorageDataSourceImpl): StorageDataSource
}