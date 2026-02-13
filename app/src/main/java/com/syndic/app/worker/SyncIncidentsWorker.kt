package com.syndic.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.IncidentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncIncidentsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val incidentRepository: IncidentRepository,
    private val communityRepository: CommunityRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Sync Incidents
            val incidentResult = incidentRepository.syncIncidents()

            // 2. Sync Blog Posts (Added for Community Engine)
            val blogResult = communityRepository.syncPosts()

            if (incidentResult.isSuccess && blogResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
