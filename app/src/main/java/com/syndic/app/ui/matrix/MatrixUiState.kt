package com.syndic.app.ui.matrix

data class MatrixItemUiState(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val apartmentNumber: String,
    val balance: Double,
    val isUpToDate: Boolean // Green if true, Red if false
)

data class MatrixUiState(
    val isLoading: Boolean = true,
    val residents: List<MatrixItemUiState> = emptyList(),
    val error: String? = null
)
