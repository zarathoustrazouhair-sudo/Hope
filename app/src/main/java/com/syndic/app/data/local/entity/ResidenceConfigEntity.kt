package com.syndic.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residence_config")
data class ResidenceConfigEntity(
    @PrimaryKey
    val id: String = "config_v1", // Singleton config
    val residenceName: String,
    val address: String = "",
    val logoUrl: String? = null,

    // Financial Configuration
    val monthlyFee: Double = 0.0,
    val conciergeSalary: Double = 0.0,
    val cleaningCost: Double = 0.0,
    val maintenanceCost: Double = 0.0,
    val otherFixedCosts: Double = 0.0,

    // Infrastructure
    val totalApartments: Int = 0,
    val currency: String = "DH",

    // Security (SHA-256 Hashes)
    val masterPinHash: String? = null, // For Admin/Syndic critical actions
    val syndicPinHash: String? = null, // Quick access for Syndic
    val conciergePinHash: String? = null, // Quick access for Concierge

    // State
    val isSetupComplete: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
