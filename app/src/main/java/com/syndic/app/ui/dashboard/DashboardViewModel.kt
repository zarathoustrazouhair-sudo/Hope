package com.syndic.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadFinancialData()
        loadCommunityData()
    }

    private fun loadFinancialData() {
        viewModelScope.launch {
            transactionRepository.getGlobalBalance()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collectLatest { balance ->
                    val runway = try { transactionRepository.getRunway() } catch (e: Exception) { 0.0 }
                    val recovery = try { transactionRepository.getRecoveryRate() } catch (e: Exception) { 0.0 }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            globalBalance = balance,
                            runwayMonths = runway,
                            recoveryRate = recovery
                        )
                    }
                }
        }
    }

    private fun loadCommunityData() {
        viewModelScope.launch {
            // Collecting incidents flow to update count reactively
            communityRepository.getAllIncidents().collectLatest { incidents ->
                val openCount = incidents.count { it.status == com.syndic.app.data.local.entity.IncidentStatus.OPEN }
                _uiState.update { it.copy(openIncidentsCount = openCount) }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = transactionRepository.syncTransactions()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }
}
