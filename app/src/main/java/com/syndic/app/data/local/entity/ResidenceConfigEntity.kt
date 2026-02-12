package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "residence_config")
data class ResidenceConfigEntity(
    @PrimaryKey
    val id: String, // UUID
    val residenceName: String,
    val logoUrl: String?,
    val monthlyFee: Double, // Room supports primitives, BigDecimal usually converted to String or Double
    val createdAt: Date?,
    val updatedAt: Date?
)
