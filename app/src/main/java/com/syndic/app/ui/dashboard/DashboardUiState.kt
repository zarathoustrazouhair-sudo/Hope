package com.syndic.app.ui.dashboard

data class DashboardUiState(
    val isLoading: Boolean = true,
    val globalBalance: Double = 0.0,
    val runwayMonths: Double = 0.0,
    val recoveryRate: Double = 0.0,
    val error: String? = null
)
