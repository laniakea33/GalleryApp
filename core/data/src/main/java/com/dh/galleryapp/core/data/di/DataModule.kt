package com.dh.galleryapp.core.data.di

import com.dh.galleryapp.core.data.repository.RepositoryImpl
import com.dh.galleryapp.core.data.repository.mock.MockRepository
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
    fun bindsReal(impl: RepositoryImpl): com.dh.galleryapp.core.domain.repository.Repository

    @Binds
    @Mock
    fun bindsMock(impl: MockRepository): com.dh.galleryapp.core.domain.repository.Repository
}