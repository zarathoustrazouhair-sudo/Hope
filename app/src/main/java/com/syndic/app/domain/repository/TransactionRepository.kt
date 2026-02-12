package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    fun getGlobalBalance(): Flow<Double>
    fun getUserBalance(userId: String): Flow<Double>
    suspend fun getRunway(): Double // In months
    suspend fun getRecoveryRate(): Double // Percentage

    // For syncing/inserting
    suspend fun createTransaction(userId: String?, amount: Double, type: TransactionType, label: String): Result<Unit>
    suspend fun syncTransactions(): Result<Unit>

    // For Workers
    suspend fun hasCotisationForMonth(userId: String, monthDate: Date): Boolean
}
