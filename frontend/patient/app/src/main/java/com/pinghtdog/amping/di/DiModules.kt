package com.pinghtdog.amping.di

import com.pinghtdog.amping.data.repository.GabbyRepository
import com.pinghtdog.amping.data.repository.GabbyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGabbyRepository(
        gabbyRepositoryImpl: GabbyRepositoryImpl
    ): GabbyRepository
}
