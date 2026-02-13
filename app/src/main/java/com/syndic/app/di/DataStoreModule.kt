package com.syndic.app.di

import com.syndic.app.data.local.datastore.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppPreferences(appPreferences: AppPreferences): AppPreferences {
        return appPreferences
    }
}
