package com.syndic.app.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.PaymentMethod
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.domain.service.PdfService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val pdfService: PdfService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadResidents()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine flows? Or collect separately.
            // Transaction List
            launch {
                transactionRepository.getAllTransactions().collectLatest { txs ->
                    _uiState.update { it.copy(transactions = txs) }
                }
            }
            // Global Balance
            launch {
                transactionRepository.getGlobalBalance().collectLatest { balance ->
                    _uiState.update { it.copy(globalBalance = balance) }
                }
            }
        }
    }

    private fun loadResidents() {
        viewModelScope.launch {
            try {
                val residents = userRepository.getAllUsers()
                    .filter { it.apartmentNumber.startsWith("AP") }
                    .sortedBy { it.apartmentNumber.replace("AP", "").toIntOrNull() ?: 999 }
                _uiState.update { it.copy(residents = residents) }
            } catch (e: Exception) {
                // Ignore failure for dropdown for now or show empty
            }
        }
    }

    fun createIncome(residentId: String, amount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Get resident info for label
            val resident = _uiState.value.residents.find { it.id == residentId }
            val label = "Paiement ${resident?.apartmentNumber ?: residentId}"

            val result = transactionRepository.createTransaction(
                userId = residentId,
                amount = amount,
                type = TransactionType.PAIEMENT,
                label = label,
                paymentMethod = method
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                // Flow updates list automatically
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Erreur encaissement: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun createExpense(amount: Double, provider: String, category: String) {
        viewModelScope.launch {
             _uiState.update { it.copy(isLoading = true, error = null) }

             // Validate inputs
             if (provider.isBlank() || category.isBlank()) {
                 _uiState.update { it.copy(isLoading = false, error = "Prestataire et Catégorie obligatoires") }
                 return@launch
             }

             val result = transactionRepository.createTransaction(
                 userId = null, // Global expense
                 amount = amount,
                 type = TransactionType.DEPENSE,
                 label = "Dépense: $category",
                 provider = provider,
                 category = category
             )

             if (result.isSuccess) {
                 _uiState.update { it.copy(isLoading = false) }
             } else {
                 _uiState.update { it.copy(isLoading = false, error = "Erreur dépense: ${result.exceptionOrNull()?.message}") }
             }
        }
    }

    fun generatePdf(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, pdfFile = null) }

            val result = if (transaction.type == TransactionType.PAIEMENT) {
                pdfService.generateReceipt(transaction)
            } else if (transaction.type == TransactionType.DEPENSE) {
                pdfService.generateExpenseVoucher(transaction)
            } else {
                Result.failure(IllegalArgumentException("Type de transaction non supporté pour PDF"))
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, pdfFile = result.getOrNull()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Erreur PDF: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearPdfState() {
        _uiState.update { it.copy(pdfFile = null) }
    }

    fun clearError() {
         _uiState.update { it.copy(error = null) }
    }
}
