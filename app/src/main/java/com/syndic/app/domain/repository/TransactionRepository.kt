package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.PaymentMethod
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    fun getGlobalBalance(): Flow<Double>
    fun getUserBalance(userId: String): Flow<Double>
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun getRunway(): Double // In months
    suspend fun getRecoveryRate(): Double // Percentage

    // For syncing/inserting
    suspend fun createTransaction(
        userId: String?,
        amount: Double,
        type: TransactionType,
        label: String,
        paymentMethod: PaymentMethod? = null,
        provider: String? = null,
        category: String? = null,
        receiptPath: String? = null
    ): Result<TransactionEntity> // Return Entity so we can generate PDF

    suspend fun syncTransactions(): Result<Unit>

    // For Workers
    suspend fun hasCotisationForMonth(userId: String, monthDate: Date): Boolean
}
