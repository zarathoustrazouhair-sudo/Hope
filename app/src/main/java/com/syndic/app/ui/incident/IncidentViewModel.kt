package com.syndic.app.ui.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            incidentRepository.getAllIncidents()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { incidents ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            incidents = incidents
                        )
                    }
                }
        }
    }

    fun createIncident(title: String, description: String, photoUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = incidentRepository.createIncident(title, description, photoUrl)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to create incident"
                    )
                }
            } else {
                // Success: Flow will update list automatically. Loading state will clear on next emission.
            }
        }
    }

    fun refreshIncidents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = incidentRepository.syncIncidents()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }
}
