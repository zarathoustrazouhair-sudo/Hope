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
    val id: String,
    val apartment: String,
    val name: String,
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
            val config = configRepository.getConfig().firstOrNull()
            val monthlyFee = config?.monthlyFee ?: 200.0 // Default fallback

            // Filter only residents AP1-AP15 or similar pattern
            // Sort by Apartment Number
            val residents = allUsers.filter { it.role.name == "RESIDENT" }
                .sortedBy {
                    it.apartmentNumber.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 999
                }

            val statuses = residents.map { user ->
                // Collect the first value of the Flow
                val userBalanceFlow = transactionRepository.getUserBalance(user.id)
                val balance = try {
                    userBalanceFlow.firstOrNull() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }

                // Tricolor Logic:
                // GOLD: Solde > 3 * Monthly Fee (3 months advance)
                // GREEN: Solde >= 0 (Up to date)
                // RED: Solde < 0 (In Debt)

                val color = when {
                    balance > (monthlyFee * 3) && monthlyFee > 0 -> MatrixColor.GOLD
                    balance >= 0 -> MatrixColor.GREEN
                    else -> MatrixColor.RED
                }

                ResidentStatus(
                    id = user.id,
                    apartment = user.apartmentNumber,
                    name = "${user.firstName} ${user.lastName}",
                    balance = balance,
                    statusColor = color
                )
            }

            _state.value = MatrixState(residents = statuses, isLoading = false)
        }
    }
}
