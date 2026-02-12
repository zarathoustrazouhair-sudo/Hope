package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.ResidenceConfigEntity
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun getConfig(): Flow<ResidenceConfigEntity?>
    suspend fun saveConfig(config: ResidenceConfigEntity)
    suspend fun isSetupComplete(): Boolean
}
