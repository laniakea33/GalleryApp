package com.dh.galleryapp.core.network.di

import com.dh.galleryapp.core.network.NetworkDataSource
import com.dh.galleryapp.core.network.NetworkDataSourceImpl
import com.dh.galleryapp.core.network.retrofit.ImageApi
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    abstract fun binds(impl: NetworkDataSourceImpl): NetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideImageApi(retrofit: Retrofit) = retrofit.create(ImageApi::class.java)

    @Provides
    fun provideRetrofit(moshi: Moshi) = Retrofit.Builder()
        .baseUrl("https://picsum.photos/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}