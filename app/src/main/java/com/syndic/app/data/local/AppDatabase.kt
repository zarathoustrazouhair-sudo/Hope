package com.syndic.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.syndic.app.data.local.converter.RoomTypeConverters
import com.syndic.app.data.local.dao.BlogDao
import com.syndic.app.data.local.dao.IncidentDao
import com.syndic.app.data.local.dao.ProviderDao
import com.syndic.app.data.local.dao.ResidenceConfigDao
import com.syndic.app.data.local.dao.TaskDao
import com.syndic.app.data.local.dao.TransactionDao
import com.syndic.app.data.local.dao.UserDao
import com.syndic.app.data.local.entity.BlogPostEntity
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.ProviderEntity
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.TaskEntity
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        IncidentEntity::class,
        ResidenceConfigEntity::class,
        TransactionEntity::class,
        BlogPostEntity::class,
        TaskEntity::class,
        ProviderEntity::class
    ],
    version = 8, // Bumped for Phase 10 (Tasks, Providers, Config V8)
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun incidentDao(): IncidentDao
    abstract fun residenceConfigDao(): ResidenceConfigDao
    abstract fun transactionDao(): TransactionDao
    abstract fun blogDao(): BlogDao
    abstract fun taskDao(): TaskDao
    abstract fun providerDao(): ProviderDao
}
