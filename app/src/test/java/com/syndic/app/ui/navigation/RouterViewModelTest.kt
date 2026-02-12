package com.syndic.app.ui.navigation

import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when setup is not complete, state reflects it`() = runTest {
        // Arrange
        val configRepository = mockk<ConfigRepository>()
        coEvery { configRepository.isSetupComplete() } returns false

        // Act
        val viewModel = RouterViewModel(configRepository)

        // Assert
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(false, viewModel.state.value.isSetupComplete)
    }

    @Test
    fun `when setup is complete, state reflects it`() = runTest {
        // Arrange
        val configRepository = mockk<ConfigRepository>()
        coEvery { configRepository.isSetupComplete() } returns true

        // Act
        val viewModel = RouterViewModel(configRepository)

        // Assert
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(true, viewModel.state.value.isSetupComplete)
    }
}
