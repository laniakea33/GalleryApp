package com.dh.galleryapp.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Named("diskCacheDir")
    fun provideCacheDiskCacheDir(@ApplicationContext context: Context): String =
        context.externalCacheDir!!.absolutePath

    @Provides
    @Named("journalFileDir")
    fun provideJournalFileDir(@ApplicationContext context: Context): String =
        context.filesDir.absolutePath
}