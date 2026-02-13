package com.syndic.app.ui.community.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = userRepository.getCurrentUser().firstOrNull() ?: return@launch
            val isSyndic = user.role == UserRole.SYNDIC || user.role == UserRole.ADJOINT

            _uiState.update { it.copy(isSyndic = isSyndic) }

            if (isSyndic) {
                // Syndic sees all incidents
                communityRepository.getAllIncidents().collectLatest { incidents ->
                    _uiState.update { it.copy(incidents = incidents, isLoading = false) }
                }
            } else {
                // Resident sees only their own incidents
                communityRepository.getUserIncidents(user.id).collectLatest { incidents ->
                    _uiState.update { it.copy(incidents = incidents, isLoading = false) }
                }
            }
        }
    }

    fun createIncident(title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userRepository.getCurrentUser().firstOrNull()

            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "Utilisateur non trouvé") }
                return@launch
            }

            val result = communityRepository.createIncident(
                userId = user.id,
                title = title,
                description = description,
                photoUrl = null // No heavy image support for now
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun updateStatus(incidentId: String, newStatus: IncidentStatus) {
        viewModelScope.launch {
            // Optimistic update or wait for flow? Wait for flow safer.
            val result = communityRepository.updateIncidentStatus(incidentId, newStatus)
             if (result.isFailure) {
                _uiState.update { it.copy(error = "Erreur mise à jour statut") }
            }
        }
    }
}
