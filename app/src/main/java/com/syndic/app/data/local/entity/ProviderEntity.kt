package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val phone: String,
    val cin: String? = null,
    val category: String, // Plomberie, Electricité, etc.
    val createdAt: Long = System.currentTimeMillis()
)
