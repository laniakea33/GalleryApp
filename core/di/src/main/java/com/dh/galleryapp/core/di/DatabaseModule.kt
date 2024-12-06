package com.dh.galleryapp.core.di

import android.content.Context
import com.dh.galleryapp.core.database.ImageDao
import com.dh.galleryapp.core.database.ImageDatabase
import com.dh.galleryapp.core.database.LocalDataSource
import com.dh.galleryapp.core.database.LocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun binds(impl: LocalDataSourceImpl): LocalDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDao(@ApplicationContext context: Context): ImageDao =
        ImageDatabase.getInstance(context).imageDao()

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ImageDatabase =
        ImageDatabase.getInstance(context)
}