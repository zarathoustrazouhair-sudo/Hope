package com.syndic.app.ui.finance

import com.syndic.app.data.local.entity.PaymentMethod
import com.syndic.app.data.local.entity.TransactionEntity
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.domain.service.PdfService
import com.syndic.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactionRepository = mockk<TransactionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val pdfService = mockk<PdfService>()

    private fun createViewModel() = FinanceViewModel(transactionRepository, userRepository, pdfService)

    @Test
    fun `loadData fetches transactions and balance`() = runTest {
        // Arrange
        val txs = listOf(
            TransactionEntity("1", null, 100.0, TransactionType.PAIEMENT, "Income", null, null, null, null, Date(), Date())
        )
        coEvery { transactionRepository.getAllTransactions() } returns flowOf(txs)
        coEvery { transactionRepository.getGlobalBalance() } returns flowOf(500.0)
        coEvery { userRepository.getAllUsers() } returns emptyList()

        // Act
        val viewModel = createViewModel()

        // Assert (Need to wait for flow collection? UnconfinedTestDispatcher handles it if flows emit immediately)
        assertEquals(txs, viewModel.uiState.value.transactions)
        assertEquals(500.0, viewModel.uiState.value.globalBalance, 0.0)
    }

    @Test
    fun `createIncome success`() = runTest {
        // Arrange
        val resident = UserEntity("r1", "email", "First", "Last", UserRole.RESIDENT, null, null, null, "A", "AP1", null, null, null)
        coEvery { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        coEvery { transactionRepository.getGlobalBalance() } returns flowOf(0.0)
        coEvery { userRepository.getAllUsers() } returns listOf(resident)

        coEvery {
            transactionRepository.createTransaction(any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(mockk())

        val viewModel = createViewModel()

        // Pre-load residents (happens in init)
        // Ensure state has residents
        assertEquals(1, viewModel.uiState.value.residents.size)

        // Act
        viewModel.createIncome("r1", 100.0, PaymentMethod.CASH)

        // Assert
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `createExpense success`() = runTest {
        // Arrange
        coEvery { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        coEvery { transactionRepository.getGlobalBalance() } returns flowOf(0.0)
        coEvery { userRepository.getAllUsers() } returns emptyList()

        coEvery {
            transactionRepository.createTransaction(any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(mockk())

        val viewModel = createViewModel()

        // Act
        viewModel.createExpense(50.0, "Plumber", "Fix leak")

        // Assert
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `generatePdf success`() = runTest {
        // Arrange
        val tx = TransactionEntity("1", null, 100.0, TransactionType.PAIEMENT, "Income", null, null, null, null, Date(), Date())
        val file = File("path/to/pdf")

        coEvery { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        coEvery { transactionRepository.getGlobalBalance() } returns flowOf(0.0)
        coEvery { userRepository.getAllUsers() } returns emptyList()

        coEvery { pdfService.generateReceipt(tx) } returns Result.success(file)

        val viewModel = createViewModel()

        // Act
        viewModel.generatePdf(tx)

        // Assert
        assertEquals(file, viewModel.uiState.value.pdfFile)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
