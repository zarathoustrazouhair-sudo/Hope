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

    // Financial Configuration (La Mondiale Edition)
    val monthlyFee: Double = 0.0,
    val conciergeSalary: Double = 0.0,
    val cleaningCost: Double = 0.0,
    val electricityCost: Double = 0.0,
    val waterCost: Double = 0.0,
    val elevatorCost: Double = 0.0,
    val insuranceCost: Double = 0.0,
    val diversCost: Double = 0.0, // Replaces otherFixedCosts/maintenanceCost

    // Infrastructure
    val totalApartments: Int = 0,
    val currency: String = "DH",

    // Security (SHA-256 Hashes)
    val masterPinHash: String? = null,
    val syndicPinHash: String? = null,
    val conciergePinHash: String? = null,

    // State (Legacy, moving to DataStore but kept for entity integrity if needed,
    // or we can deprecate it. Keeping it false/true as backup).
    val isSetupComplete: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
