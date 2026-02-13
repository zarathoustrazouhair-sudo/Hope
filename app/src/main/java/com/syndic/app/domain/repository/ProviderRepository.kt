package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {
    fun getAllProviders(): Flow<List<ProviderEntity>>
    suspend fun createProvider(name: String, phone: String, category: String, cin: String?): Result<Unit>
}
