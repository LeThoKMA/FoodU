package com.example.footu.hilt

import android.content.Context
import com.example.footu.MyPreferencee
import com.example.footu.dagger2.App
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun getAppInstance(@ApplicationContext context: Context): App {
        return (context as App)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ModuleRepository {
    @Singleton
    @Provides
    fun provideRepository(): Repository {
        return Repository()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SharePrefModule {
    @Singleton
    @Provides
    fun provideSharePref(@ApplicationContext context: Context): MyPreferencee {
        return MyPreferencee(context)
    }
}
