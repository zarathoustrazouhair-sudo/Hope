package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class IncidentStatus {
    OPEN, IN_PROGRESS, RESOLVED
}

enum class IncidentPriority {
    LOW, NORMAL, URGENT
}

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey
    val id: String, // UUID
    val userId: String, // References UserEntity.id
    val title: String,
    val description: String,
    val photoUrl: String?,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val priority: IncidentPriority = IncidentPriority.NORMAL,
    val createdAt: Date?,
    val updatedAt: Date?
)
