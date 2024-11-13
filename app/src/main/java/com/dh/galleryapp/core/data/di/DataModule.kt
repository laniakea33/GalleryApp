package com.dh.galleryapp.core.data.di

import com.dh.galleryapp.core.data.repository.Repository
import com.dh.galleryapp.core.data.repository.RepositoryImpl
import com.dh.galleryapp.core.data.repository.mock.MockRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class OnlineRepository

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MockRepository

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @OnlineRepository
    fun binds(impl: RepositoryImpl): Repository

    @Binds
    @MockRepository
    fun bindsMock(impl: MockRepositoryImpl): Repository
}