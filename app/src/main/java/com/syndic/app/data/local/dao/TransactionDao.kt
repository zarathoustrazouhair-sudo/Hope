package com.syndic.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactionsSync(): List<TransactionEntity> // For suspend functions

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    fun getUserTransactions(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Financial Idempotence Check: Count transactions of a specific type for a user within a date range
    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND type = :type AND date >= :startDate AND date <= :endDate")
    suspend fun countTransactionsInRange(
        userId: String,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Int
}
