package com.syndic.app.ui.dashboard

import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactionRepository = mockk<TransactionRepository>()
    private val communityRepository = mockk<CommunityRepository>()

    private fun createViewModel() = DashboardViewModel(transactionRepository, communityRepository)

    @Test
    fun `runway calculation propagates to UI state`() = runTest {
        // Arrange
        coEvery { transactionRepository.getGlobalBalance() } returns flowOf(5000.0)
        coEvery { transactionRepository.getRunway() } returns 2.5
        coEvery { transactionRepository.getRecoveryRate() } returns 80.0
        coEvery { communityRepository.getAllIncidents() } returns flowOf(emptyList())

        // Act
        val viewModel = createViewModel()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(5000.0, state.globalBalance, 0.0)
        assertEquals(2.5, state.runwayMonths, 0.0)
        assertEquals(80.0, state.recoveryRate, 0.0)
    }
}
