package com.syndic.app.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.syndic.app.data.local.dao.IncidentDao
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.IncidentPriority
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.domain.repository.IncidentRepository
import com.syndic.app.worker.UploadIncidentWorker
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Serializable
data class IncidentDto(
    val id: String,
    val title: String,
    val description: String,
    val photo_url: String?,
    val status: String,
    val priority: String,
    val user_id: String,
    val created_at: String?,
    val updated_at: String?
)

class IncidentRepositoryImpl @Inject constructor(
    private val incidentDao: IncidentDao,
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val workManager: WorkManager
) : IncidentRepository {

    override fun getAllIncidents(): Flow<List<IncidentEntity>> {
        return incidentDao.getAllIncidents()
    }

    override suspend fun getIncident(id: String): IncidentEntity? {
        return incidentDao.getIncidentById(id)
    }

    override suspend fun createIncident(title: String, description: String, photoUrl: String?): Result<Unit> {
        val currentUser = auth.currentUserOrNull() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val incidentId = UUID.randomUUID().toString()
            val now = Date()
            val incident = IncidentEntity(
                id = incidentId,
                userId = currentUser.id,
                title = title,
                description = description,
                photoUrl = photoUrl,
                status = IncidentStatus.OPEN,
                priority = IncidentPriority.NORMAL,
                createdAt = now,
                updatedAt = now
            )

            // Offline-First: Save locally immediately
            incidentDao.insertIncident(incident)

            // Schedule immediate upload with retry
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadWork = OneTimeWorkRequestBuilder<UploadIncidentWorker>()
                .setInputData(workDataOf("incident_id" to incidentId))
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueue(uploadWork)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncIncidents(): Result<Unit> {
        return try {
            // Fetch from Supabase
            val result = postgrest.from("incidents").select().decodeList<IncidentDto>()

            val entities = result.map { dto ->
                IncidentEntity(
                    id = dto.id,
                    userId = dto.user_id,
                    title = dto.title,
                    description = dto.description,
                    photoUrl = dto.photo_url,
                    status = try { IncidentStatus.valueOf(dto.status) } catch (e: Exception) { IncidentStatus.OPEN },
                    priority = try { IncidentPriority.valueOf(dto.priority) } catch (e: Exception) { IncidentPriority.NORMAL },
                    createdAt = dto.created_at?.let { Date.from(Instant.parse(it)) },
                    updatedAt = dto.updated_at?.let { Date.from(Instant.parse(it)) }
                )
            }

            // Save to Local DB (Single Source of Truth)
            if (entities.isNotEmpty()) {
                incidentDao.insertIncidents(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadIncident(incident: IncidentEntity): Result<Unit> {
        return try {
            val dto = IncidentDto(
                id = incident.id,
                title = incident.title,
                description = incident.description,
                photo_url = incident.photoUrl,
                status = incident.status.name,
                priority = incident.priority.name,
                user_id = incident.userId,
                created_at = incident.createdAt?.toInstant()?.toString(),
                updated_at = incident.updatedAt?.toInstant()?.toString()
            )

            postgrest.from("incidents").upsert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
