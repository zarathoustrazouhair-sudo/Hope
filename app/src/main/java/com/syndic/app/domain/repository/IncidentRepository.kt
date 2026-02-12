package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    fun getAllIncidents(): Flow<List<IncidentEntity>>
    suspend fun getIncident(id: String): IncidentEntity?
    suspend fun createIncident(title: String, description: String, photoUrl: String?): Result<Unit>
    suspend fun syncIncidents(): Result<Unit>
    suspend fun uploadIncident(incident: IncidentEntity): Result<Unit>
}
