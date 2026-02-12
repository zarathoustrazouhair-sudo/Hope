package com.syndic.app.di

import android.content.Context
import androidx.room.Room
import com.syndic.app.data.local.AppDatabase
import com.syndic.app.data.local.dao.IncidentDao
import com.syndic.app.data.local.dao.ResidenceConfigDao
import com.syndic.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "syndic_app.db"
        ).fallbackToDestructiveMigration() // For MVP simplicity
         .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideIncidentDao(database: AppDatabase): IncidentDao = database.incidentDao()

    @Provides
    fun provideResidenceConfigDao(database: AppDatabase): ResidenceConfigDao = database.residenceConfigDao()
}
