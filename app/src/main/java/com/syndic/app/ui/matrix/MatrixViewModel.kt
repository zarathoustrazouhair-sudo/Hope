package com.syndic.app.ui.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatrixViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatrixUiState())
    val uiState: StateFlow<MatrixUiState> = _uiState.asStateFlow()

    init {
        loadMatrix()
    }

    private fun loadMatrix() {
        viewModelScope.launch {
            try {
                // Fetch all residents (assuming they are synced locally)
                // Filter for RESIDENT role (or include all if needed for matrix)
                val users = userRepository.getAllUsers()
                    .filter { it.role == UserRole.RESIDENT }
                    .sortedBy { it.apartmentNumber.toIntOrNull() ?: 999 } // Sort by Apartment Number

                val matrixItems = users.map { user ->
                    // Fetch balance for each user
                    val balance = transactionRepository.getUserBalance(user.id).first()

                    MatrixItemUiState(
                        userId = user.id,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        apartmentNumber = user.apartmentNumber,
                        balance = balance,
                        isUpToDate = balance >= 0
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        residents = matrixItems
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
