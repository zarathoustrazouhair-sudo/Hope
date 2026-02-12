package com.syndic.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.syndic.app.data.local.converter.RoomTypeConverters
import com.syndic.app.data.local.dao.IncidentDao
import com.syndic.app.data.local.dao.ResidenceConfigDao
import com.syndic.app.data.local.dao.TransactionDao
import com.syndic.app.data.local.dao.UserDao
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        IncidentEntity::class,
        ResidenceConfigEntity::class,
        TransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun incidentDao(): IncidentDao
    abstract fun residenceConfigDao(): ResidenceConfigDao
    abstract fun transactionDao(): TransactionDao
}
