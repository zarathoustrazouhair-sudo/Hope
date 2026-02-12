package com.syndic.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val session = auth.currentSessionOrNull()
            if (session != null) {
                // User is authenticated, load profile to get role
                loadUserProfile()
            } else {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            userRole = user.role
                        )
                    }
                } else {
                    // Authenticated but no local profile yet, try syncing
                    syncProfile()
                }
            }
        }
    }

    private suspend fun syncProfile() {
        val result = userRepository.syncUser()
        if (result.isFailure) {
             _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${result.exceptionOrNull()?.message}"
                )
            }
        }
        // If success, flow above will trigger update
    }

    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                // Auth success, logic continues in checkSession/loadUserProfile via flow
                loadUserProfile()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Login failed")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _uiState.update { AuthUiState() } // Reset state
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
