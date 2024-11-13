package com.dh.galleryapp.core.data.di

import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun binds(impl: RepositoryImpl): Repository
}