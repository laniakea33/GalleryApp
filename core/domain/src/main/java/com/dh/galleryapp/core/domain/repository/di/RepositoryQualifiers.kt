package com.dh.galleryapp.core.domain.repository.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Real

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock