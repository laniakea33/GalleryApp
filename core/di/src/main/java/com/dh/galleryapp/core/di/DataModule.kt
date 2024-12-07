package com.dh.galleryapp.core.di

import com.dh.galleryapp.core.data.repository.ImageRepositoryImpl
import com.dh.galleryapp.core.data.repository.mock.MockImageRepository
import com.dh.galleryapp.core.domain.repository.ImageRepository
import com.dh.galleryapp.core.domain.repository.di.Mock
import com.dh.galleryapp.core.domain.repository.di.Real
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Real
    fun bindsReal(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Mock
    fun bindsMock(impl: MockImageRepository): ImageRepository
}