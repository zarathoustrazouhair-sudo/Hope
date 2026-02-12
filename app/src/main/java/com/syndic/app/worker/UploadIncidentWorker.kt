package com.syndic.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.syndic.app.domain.repository.IncidentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UploadIncidentWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val incidentRepository: IncidentRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val incidentId = inputData.getString("incident_id") ?: return@withContext Result.failure()

        try {
            val incident = incidentRepository.getIncident(incidentId) ?: return@withContext Result.failure()

            // Upload to Supabase
            val result = incidentRepository.uploadIncident(incident)

            if (result.isSuccess) {
                // Mark as synced locally if we had a flag
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
