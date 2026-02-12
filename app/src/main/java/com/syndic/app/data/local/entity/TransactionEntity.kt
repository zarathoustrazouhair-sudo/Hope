package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    COTISATION, PAIEMENT, DEPENSE
}

enum class PaymentMethod {
    CASH, CHEQUE, VIREMENT, CARTE, AUTRE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String, // UUID
    val userId: String?, // Maps to UserEntity.id, nullable for global expenses
    val amount: Double, // Room stores primitives, use Double for MVP simplicity (BigDecimal requires TypeConverter)
    val type: TransactionType,
    val label: String,

    // New Fields for Phase 5 Finance Engine
    val paymentMethod: PaymentMethod? = null, // Mandatory for PAIEMENT
    val provider: String? = null, // Mandatory for DEPENSE
    val category: String? = null, // Mandatory for DEPENSE (e.g. "Plomberie")
    val receiptPath: String? = null, // Path to generated PDF file

    val date: Date,
    val createdAt: Date?
)
