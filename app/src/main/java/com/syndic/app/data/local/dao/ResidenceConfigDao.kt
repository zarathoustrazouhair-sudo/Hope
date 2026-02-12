package com.syndic.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidenceConfigDao {
    @Query("SELECT * FROM residence_config LIMIT 1")
    fun getConfig(): Flow<ResidenceConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ResidenceConfigEntity)
}
