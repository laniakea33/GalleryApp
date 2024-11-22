package com.dh.galleryapp.core.data.di

import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.data.repository.RepositoryImpl
import com.dh.galleryapp.core.data.repository.mock.MockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Real

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Real
    fun bindsReal(impl: RepositoryImpl): Repository

    @Binds
    @Mock
    fun bindsMock(impl: MockRepository): Repository
}