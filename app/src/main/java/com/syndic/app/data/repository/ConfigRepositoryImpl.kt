package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.ResidenceConfigDao
import com.syndic.app.data.local.datastore.AppPreferences
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val dao: ResidenceConfigDao,
    private val appPreferences: AppPreferences
) : ConfigRepository {

    override fun getConfig(): Flow<ResidenceConfigEntity?> {
        return dao.getConfig()
    }

    override suspend fun saveConfig(config: ResidenceConfigEntity) {
        dao.insertConfig(config)
        // Sync with DataStore if config is marked complete
        if (config.isSetupComplete) {
            appPreferences.setSetupDone(true)
        }
    }

    override suspend fun isSetupComplete(): Boolean {
        // Source of Truth is now DataStore as per TEP
        return appPreferences.isSetupDone.first()
    }
}
