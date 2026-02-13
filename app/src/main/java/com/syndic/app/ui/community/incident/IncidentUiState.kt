package com.syndic.app.ui.community.incident

import com.syndic.app.data.local.entity.IncidentEntity

data class IncidentUiState(
    val incidents: List<IncidentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyndic: Boolean = false
)
