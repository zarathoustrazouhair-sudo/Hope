package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.ResidenceConfigDao
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val dao: ResidenceConfigDao
) : ConfigRepository {

    override fun getConfig(): Flow<ResidenceConfigEntity?> {
        return dao.getConfig()
    }

    override suspend fun saveConfig(config: ResidenceConfigEntity) {
        dao.insertConfig(config)
    }

    override suspend fun isSetupComplete(): Boolean {
        val config = dao.getConfig().firstOrNull()
        return config?.isSetupComplete == true
    }
}
