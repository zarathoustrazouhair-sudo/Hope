package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.TransactionDao
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.domain.repository.TransactionRepository
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@Serializable
data class TransactionDto(
    val id: String,
    val user_id: String?,
    val amount: Double,
    val type: String,
    val label: String,
    val date: String,
    val created_at: String? = null
)

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val postgrest: Postgrest,
    private val auth: Auth
) : TransactionRepository {

    override fun getGlobalBalance(): Flow<Double> {
        // Solde GLOBAL (Caisse) = Somme(PAIEMENTS de tous les résidents) + Somme(DEPENSES globales)
        return transactionDao.getGlobalBalance().map { it ?: 0.0 }
    }

    override fun getUserBalance(userId: String): Flow<Double> {
        // Solde RÉSIDENT = Somme(COTISATIONS) + Somme(PAIEMENTS)
        return transactionDao.getUserBalance(userId).map { it ?: 0.0 }
    }

    override suspend fun getRunway(): Double {
        // Formule : (Solde Global Trésorerie) / (Moyenne des transactions de type 'DEPENSE' des 3 derniers mois).
        val currentBalance = getGlobalBalance().first()

        val threeMonthsAgo = Date.from(Instant.now().minus(90, ChronoUnit.DAYS))
        val recentExpenses = transactionDao.getExpensesSince(threeMonthsAgo)

        if (recentExpenses.isEmpty()) {
            return Double.MAX_VALUE // Infinite runway
        }

        val totalExpenses = recentExpenses.sumOf { abs(it.amount) } // Ensure positive magnitude for calculation
        val monthlyAverage = totalExpenses / 3.0

        if (monthlyAverage == 0.0) return Double.MAX_VALUE

        return currentBalance / monthlyAverage
    }

    override suspend fun getRecoveryRate(): Double {
        // Taux de Recouvrement : (Total Payé / Total Dû) * 100.
        // Total Dû = Abs(Sum(COTISATION))
        // Total Payé = Sum(PAIEMENT)

        val totalDue = abs(transactionDao.getSumByType(TransactionType.COTISATION) ?: 0.0)
        val totalPaid = transactionDao.getSumByType(TransactionType.PAIEMENT) ?: 0.0

        if (totalDue == 0.0) return 100.0 // Nothing due means 100% recovered (or undefined, but 100 is safer for UI)

        return (totalPaid / totalDue) * 100.0
    }

    override suspend fun createTransaction(userId: String?, amount: Double, type: TransactionType, label: String): Result<Unit> {
        return try {
            val transactionId = UUID.randomUUID().toString()
            val now = Date()

            val entity = TransactionEntity(
                id = transactionId,
                userId = userId,
                amount = amount,
                type = type,
                label = label,
                date = now,
                createdAt = now
            )

            // Save locally
            transactionDao.insertTransaction(entity)

            // Sync to Supabase
            val dto = TransactionDto(
                id = transactionId,
                user_id = userId,
                amount = amount,
                type = type.name,
                label = label,
                date = now.toInstant().toString(),
                created_at = now.toInstant().toString()
            )

            postgrest.from("transactions").insert(dto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> {
        return try {
            val result = postgrest.from("transactions").select().decodeList<TransactionDto>()

            val entities = result.map { dto ->
                TransactionEntity(
                    id = dto.id,
                    userId = dto.user_id,
                    amount = dto.amount,
                    type = try { TransactionType.valueOf(dto.type) } catch (e: Exception) { TransactionType.COTISATION }, // Fallback risky but better than crash
                    label = dto.label,
                    date = Date.from(Instant.parse(dto.date)),
                    createdAt = dto.created_at?.let { Date.from(Instant.parse(it)) }
                )
            }

            if (entities.isNotEmpty()) {
                transactionDao.insertTransactions(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasCotisationForMonth(userId: String, monthDate: Date): Boolean {
        // Convert Date to Start and End of Month
        val localDate = monthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfMonth = Date.from(localDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
        val endOfMonth = Date.from(localDate.withDayOfMonth(localDate.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())

        val count = transactionDao.countCotisationsInMonth(userId, startOfMonth, endOfMonth)
        return count > 0
    }
}
