package com.syndic.app.ui.resident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResidentHomeState(
    val apartment: String = "",
    val balance: Double = 0.0,
    val runwayMonths: Double = 0.0,
    val isDefaultPin: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ResidentHomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResidentHomeState())
    val state: StateFlow<ResidentHomeState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val currentUser = userRepository.getCurrentUser().firstOrNull()
            if (currentUser != null) {
                val balance = transactionRepository.getUserBalance(currentUser.id).firstOrNull() ?: 0.0
                val runway = transactionRepository.getRunway()

                // Check if PIN is default "0000"
                val defaultPinHash = SecurityUtils.hashPin("0000")
                val isDefault = currentUser.pinHash == defaultPinHash

                _state.value = ResidentHomeState(
                    apartment = currentUser.apartmentNumber,
                    balance = balance,
                    runwayMonths = runway,
                    isDefaultPin = isDefault,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
