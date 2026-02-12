package com.syndic.app.ui.incident

import com.syndic.app.data.local.entity.IncidentEntity

data class IncidentUiState(
    val isLoading: Boolean = true,
    val incidents: List<IncidentEntity> = emptyList(),
    val error: String? = null
)
