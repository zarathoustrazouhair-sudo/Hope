package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

enum class TransactionType {
    COTISATION, PAIEMENT, DEPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String, // UUID
    val userId: String?, // Maps to UserEntity.id, nullable for global expenses
    val amount: Double, // Room stores primitives, use Double for MVP simplicity (BigDecimal requires TypeConverter)
    val type: TransactionType,
    val label: String,
    val date: Date,
    val createdAt: Date?
)
