package com.syndic.app.ui.auth

import com.syndic.app.data.local.entity.UserRole

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userRole: UserRole? = null,
    val error: String? = null
)
