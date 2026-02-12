package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.TransactionDao
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val configRepository: ConfigRepository
) : TransactionRepository {

    override fun getGlobalBalance(): Flow<Double> {
        return transactionDao.getAllTransactions().map { list ->
            list.sumOf {
                when (it.type) {
                    TransactionType.PAIEMENT -> it.amount
                    TransactionType.DEPENSE -> -it.amount
                    else -> 0.0
                }
            }
        }
    }

    override fun getUserBalance(userId: String): Flow<Double> {
        return transactionDao.getUserTransactions(userId).map { list ->
            list.sumOf {
                when (it.type) {
                    TransactionType.COTISATION -> -it.amount // Due
                    TransactionType.PAIEMENT -> it.amount    // Paid
                    else -> 0.0
                }
            }
        }
    }

    override suspend fun getRunway(): Double {
        val allTx = transactionDao.getAllTransactionsSync()
        val currentBalance = allTx.sumOf {
            when (it.type) {
                TransactionType.PAIEMENT -> it.amount
                TransactionType.DEPENSE -> -it.amount
                else -> 0.0
            }
        }

        val config = configRepository.getConfig().firstOrNull()
        val monthlyBurn = if (config != null) {
            config.conciergeSalary +
            config.cleaningCost +
            config.maintenanceCost +
            config.otherFixedCosts
        } else {
            0.0
        }

        if (monthlyBurn <= 0) return if (currentBalance > 0) 99.9 else 0.0

        return (currentBalance / monthlyBurn).coerceAtLeast(0.0)
    }

    override suspend fun getRecoveryRate(): Double {
        val allTx = transactionDao.getAllTransactionsSync()

        val totalDue = allTx.filter { it.type == TransactionType.COTISATION }.sumOf { it.amount }
        val totalPaid = allTx.filter { it.type == TransactionType.PAIEMENT }.sumOf { it.amount }

        if (totalDue <= 0) return 100.0

        return ((totalPaid / totalDue) * 100).coerceIn(0.0, 100.0)
    }

    override suspend fun createTransaction(
        userId: String?,
        amount: Double,
        type: TransactionType,
        label: String
    ): Result<Unit> {
        return try {
            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                type = type,
                label = label,
                date = Date(),
                createdAt = Date()
            )
            transactionDao.insertTransaction(tx)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun hasCotisationForMonth(userId: String, monthDate: Date): Boolean {
        // Calculate Start and End of the given month
        val cal = Calendar.getInstance()
        cal.time = monthDate
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startDate = cal.time

        // End of month: Add 1 month, then subtract 1ms
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val endDate = cal.time

        val count = transactionDao.countTransactionsInRange(
            userId = userId,
            type = TransactionType.COTISATION,
            startDate = startDate,
            endDate = endDate
        )
        return count > 0
    }
}
