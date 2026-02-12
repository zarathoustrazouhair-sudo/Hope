package com.syndic.app.ui.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatrixState(
    val residents: List<ResidentStatus> = emptyList(),
    val isLoading: Boolean = false // Default to false
)

data class ResidentStatus(
    val apartment: String,
    val balance: Double,
    val statusColor: MatrixColor
)

enum class MatrixColor {
    GOLD, GREEN, RED
}

@HiltViewModel
class MatrixViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MatrixState(isLoading = true))
    val state: StateFlow<MatrixState> = _state.asStateFlow()

    init {
        loadMatrix()
    }

    private fun loadMatrix() {
        viewModelScope.launch {
            val allUsers = userRepository.getAllUsers()
            // Filter only residents AP1-AP15 or similar pattern
            val residents = allUsers.filter { it.apartmentNumber.startsWith("AP") }
                .sortedBy {
                    // Safely extract number part
                    it.apartmentNumber.replace("AP", "").trim().toIntOrNull() ?: 999
                }

            val config = configRepository.getConfig().firstOrNull()
            val monthlyFee = config?.monthlyFee ?: 0.0

            val statuses = residents.map { user ->
                // Collect the first value of the Flow
                val balance = transactionRepository.getUserBalance(user.id).firstOrNull() ?: 0.0

                // Tricolor Logic:
                // GOLD: Solde > 3 * Monthly Fee (3 months advance)
                // GREEN: Solde >= 0 (Up to date)
                // RED: Solde < 0 (In Debt)

                val color = when {
                    balance >= (3 * monthlyFee) && monthlyFee > 0 -> MatrixColor.GOLD
                    balance >= 0 -> MatrixColor.GREEN
                    else -> MatrixColor.RED
                }

                ResidentStatus(
                    apartment = user.apartmentNumber,
                    balance = balance,
                    statusColor = color
                )
            }

            _state.value = MatrixState(residents = statuses, isLoading = false)
        }
    }
}
