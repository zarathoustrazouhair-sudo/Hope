package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.ProviderDao
import com.syndic.app.data.local.entity.ProviderEntity
import com.syndic.app.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepositoryImpl @Inject constructor(
    private val providerDao: ProviderDao
) : ProviderRepository {

    override fun getAllProviders(): Flow<List<ProviderEntity>> = providerDao.getAllProviders()

    override suspend fun createProvider(name: String, phone: String, category: String, cin: String?): Result<Unit> {
        return try {
            val provider = ProviderEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                category = category,
                cin = cin
            )
            providerDao.insertProvider(provider)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
