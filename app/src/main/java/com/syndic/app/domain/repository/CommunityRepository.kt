package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.BlogPostEntity
import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.IncidentStatus
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    // Blog Logic
    fun getAllPosts(): Flow<List<BlogPostEntity>>
    suspend fun createPost(title: String, content: String, authorId: String, category: String): Result<Unit>
    suspend fun syncPosts(): Result<Unit>
    suspend fun uploadPost(postId: String): Result<Unit>

    // Incident Logic (Assuming these are new or merged from a previous Incident Repo if any)
    fun getAllIncidents(): Flow<List<IncidentEntity>>
    fun getUserIncidents(userId: String): Flow<List<IncidentEntity>>
    suspend fun createIncident(userId: String, title: String, description: String, photoUrl: String?): Result<Unit>
    suspend fun updateIncidentStatus(incidentId: String, newStatus: IncidentStatus): Result<Unit>

    // Integration Logic (Dashboard)
    suspend fun getOpenIncidentsCount(): Int
    suspend fun getLatestPost(): BlogPostEntity?
}
