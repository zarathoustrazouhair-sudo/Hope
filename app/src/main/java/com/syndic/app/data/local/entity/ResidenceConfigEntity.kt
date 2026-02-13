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
    val electricityCost: Double = 0.0,
    val waterCost: Double = 0.0,
    val elevatorCost: Double = 0.0,
    val insuranceCost: Double = 0.0,
    val diversCost: Double = 0.0,

    // Infrastructure
    val totalApartments: Int = 0,
    val currency: String = "DH",

    // Security (SHA-256 Hashes)
    val masterPinHash: String? = null,
    val syndicPinHash: String? = null,
    val conciergePinHash: String? = null,

    // Phase 10: New Fields for Onboarding/Settings
    val syndicCivility: String = "Monsieur", // M./Mme/Mlle
    val syndicEmail: String = "",
    val syndicPhone: String = "", // With +212 prefix logic

    val adjointName: String = "",
    val adjointPhone: String = "",
    val adjointEmail: String = "",

    val conciergeName: String = "",
    val conciergePhone: String = "",

    val fiscalYearStartDate: Long = 0L, // Bloquer transactions antérieures

    // PDF Customization
    val stampUri: String? = null, // URI to local image
    val isAutoStampEnabled: Boolean = false,

    val isSetupComplete: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
