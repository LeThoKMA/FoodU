package com.example.footu.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule

@Module
@InstallIn(SingletonComponent::class)
object ModuleRepository {
    @Singleton
    @Provides
    fun provideRepository(): Repository {
        return Repository()
    }
}
