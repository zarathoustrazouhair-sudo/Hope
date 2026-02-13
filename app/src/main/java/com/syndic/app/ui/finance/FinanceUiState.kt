package com.syndic.app.ui.finance

import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.UserEntity

data class FinanceUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val residents: List<UserEntity> = emptyList(), // For Income dropdown
    val isLoading: Boolean = false,
    val error: String? = null,
    val pdfFile: java.io.File? = null, // Temporary hold for generated PDF to share/open
    val globalBalance: Double = 0.0
)
