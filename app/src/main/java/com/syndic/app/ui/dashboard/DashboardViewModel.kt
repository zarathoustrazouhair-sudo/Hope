package com.syndic.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadFinancialData()
    }

    private fun loadFinancialData() {
        viewModelScope.launch {
            // Global Balance is a Flow
            transactionRepository.getGlobalBalance()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { balance ->
                    // Runway and Recovery Rate are currently suspend functions (calculated snapshots)
                    // In a reactive setup, ideally they would be flows too, but for MVP we calculate them on updates.

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

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = transactionRepository.syncTransactions()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
            // Flow will update UI automatically on success
        }
    }
}
