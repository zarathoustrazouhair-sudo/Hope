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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // For Runway Calculation: Get expenses (Negative amounts) from the last 3 months
    // We filter by type DEPENSE
    @Query("SELECT * FROM transactions WHERE type = 'DEPENSE' AND date >= :threeMonthsAgo")
    suspend fun getExpensesSince(threeMonthsAgo: Date): List<TransactionEntity>

    // For Global Balance: Sum of all transactions?
    // Spec: "Solde GLOBAL (Caisse) = Somme(PAIEMENTS de tous les résidents) + Somme(DEPENSES globales)"
    // COTISATIONS are excluded from Global Balance (CASH).
    @Query("SELECT SUM(amount) FROM transactions WHERE type IN ('PAIEMENT', 'DEPENSE')")
    fun getGlobalBalance(): Flow<Double?>

    // For Resident Balance: Sum of COTISATION + PAIEMENT
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type IN ('COTISATION', 'PAIEMENT')")
    fun getUserBalance(userId: String): Flow<Double?>

    // For checking monthly debit idempotence
    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND type = 'COTISATION' AND date >= :startOfMonth AND date <= :endOfMonth")
    suspend fun countCotisationsInMonth(userId: String, startOfMonth: Date, endOfMonth: Date): Int

    // Optimized Sum Queries
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    suspend fun getSumByType(type: TransactionType): Double?
}
