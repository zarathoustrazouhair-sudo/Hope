package com.syndic.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.syndic.app.data.local.dao.UserDao
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@HiltWorker
class MonthlyDebitWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val userDao: UserDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            val localDate = LocalDate.now()
            val monthName = localDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val year = localDate.year
            val label = "Cotisation $monthName $year"
            val amount = 250.0 // Positive magnitude, Repo handles sign inversion

            // Fetch all users (residents) from local DB
            val users = userDao.getAllUsersSync()

            users.forEach { user ->
                // Check if user is a RESIDENT (or all roles pay?)
                // Assuming RESIDENT role pays.
                if (user.role == UserRole.RESIDENT) {
                    // Check idempotence: Has cotisation for this month?
                    if (!transactionRepository.hasCotisationForMonth(user.id, now)) {
                        // Create DEBIT transaction
                        transactionRepository.createTransaction(
                            userId = user.id,
                            amount = amount,
                            type = TransactionType.COTISATION,
                            label = label
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            // Log error if needed, but return failure or retry depending on strategy
            Result.failure()
        }
    }
}
