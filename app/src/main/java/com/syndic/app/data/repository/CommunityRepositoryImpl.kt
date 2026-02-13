package com.syndic.app.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.syndic.app.data.local.dao.BlogDao
import com.syndic.app.data.local.dao.IncidentDao
import com.syndic.app.data.local.entity.BlogPostEntity
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.worker.UploadPostWorker
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BlogPostDto(
    val id: String,
    val title: String,
    val content: String,
    val author_id: String,
    val category: String,
    val created_at: String?
)

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val blogDao: BlogDao,
    private val incidentDao: IncidentDao,
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val workManager: WorkManager
) : CommunityRepository {

    // --- Blog Implementation ---
    override fun getAllPosts(): Flow<List<BlogPostEntity>> {
        return blogDao.getAllPosts()
    }

    override suspend fun createPost(
        title: String,
        content: String,
        authorId: String,
        category: String
    ): Result<Unit> {
        return try {
            val postId = UUID.randomUUID().toString()
            val now = Date()

            // Auto-Signature Logic
            val signature = "\n\nPublié par SYNDIC" // Simplified for now, ideally fetch role/name
            val contentWithSignature = if (content.contains("Publié par")) content else content + signature

            val post = BlogPostEntity(
                id = postId,
                title = title,
                content = contentWithSignature,
                authorId = authorId,
                date = now,
                category = category
            )
            // Offline-First
            blogDao.insertPost(post)

            // Schedule Upload
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadWork = OneTimeWorkRequestBuilder<UploadPostWorker>()
                .setInputData(workDataOf("post_id" to postId))
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

    override suspend fun syncPosts(): Result<Unit> {
        return try {
            // Fetch from Supabase "blog_posts" table
            val result = postgrest.from("blog_posts").select().decodeList<BlogPostDto>()

            val entities = result.map { dto ->
                BlogPostEntity(
                    id = dto.id,
                    title = dto.title,
                    content = dto.content,
                    authorId = dto.author_id,
                    category = dto.category,
                    date = dto.created_at?.let { Date.from(Instant.parse(it)) } ?: Date()
                )
            }

            if (entities.isNotEmpty()) {
                blogDao.insertPosts(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadPost(postId: String): Result<Unit> {
        return try {
            val post = blogDao.getPostById(postId) ?: return Result.failure(Exception("Post not found locally"))

            val dto = BlogPostDto(
                id = post.id,
                title = post.title,
                content = post.content,
                author_id = post.authorId,
                category = post.category,
                created_at = post.date.toInstant().toString()
            )

            postgrest.from("blog_posts").upsert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Incident Implementation ---
    override fun getAllIncidents(): Flow<List<IncidentEntity>> {
        return incidentDao.getAllIncidents()
    }

    override fun getUserIncidents(userId: String): Flow<List<IncidentEntity>> {
        return incidentDao.getUserIncidents(userId)
    }

    override suspend fun createIncident(
        userId: String,
        title: String,
        description: String,
        photoUrl: String?
    ): Result<Unit> {
        return try {
            val incident = IncidentEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                description = description,
                photoUrl = photoUrl,
                status = IncidentStatus.OPEN,
                createdAt = Date(),
                updatedAt = Date()
            )
            incidentDao.insertIncident(incident)

            // Schedule Upload (Restoring immediate upload functionality from Phase 2)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadWork = OneTimeWorkRequestBuilder<com.syndic.app.worker.UploadIncidentWorker>()
                .setInputData(workDataOf("incident_id" to incident.id))
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

    override suspend fun updateIncidentStatus(incidentId: String, newStatus: IncidentStatus): Result<Unit> {
        return try {
            val incident = incidentDao.getIncidentById(incidentId)
            if (incident != null) {
                val updated = incident.copy(status = newStatus, updatedAt = Date())
                incidentDao.updateIncident(updated)
                // Trigger upload/sync for update?
                // For MVP, next sync will pick it up or we schedule upload.
                Result.success(Unit)
            } else {
                Result.failure(Exception("Incident not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOpenIncidentsCount(): Int {
        return incidentDao.getAllIncidentsSync().count { it.status == IncidentStatus.OPEN }
    }

    override suspend fun getLatestPost(): BlogPostEntity? {
        return blogDao.getAllPosts().firstOrNull()?.firstOrNull()
    }
}
