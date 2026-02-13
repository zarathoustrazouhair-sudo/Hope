package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String, // UUID
    val title: String,
    val description: String? = null,
    val dueDate: Date,
    val isRecurring: Boolean = false, // If true, regenerate next month on completion
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)
