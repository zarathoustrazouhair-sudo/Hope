package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class UserRole {
    SYNDIC, ADJOINT, CONCIERGE, RESIDENT
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String, // Maps to Supabase Auth UUID
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val phoneNumber: String?,
    val cin: String?, // Concierge only
    val mandateStartDate: Date?, // Syndic only
    val building: String,
    val apartmentNumber: String,
    val createdAt: Date?,
    val updatedAt: Date?
)
