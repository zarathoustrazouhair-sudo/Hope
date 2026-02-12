package com.syndic.app.ui.login

import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.MainDispatcherRule
import com.syndic.app.util.SecurityUtils
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `syndic login success`() = runTest {
        // Arrange
        val configRepository = mockk<ConfigRepository>()
        val userRepository = mockk<UserRepository>()
        val pin = "123456"
        val hashedPin = SecurityUtils.hashPin(pin)
        val config = ResidenceConfigEntity(
            residenceName = "Test Residence",
            masterPinHash = hashedPin,
            isSetupComplete = true
        )

        coEvery { configRepository.getConfig() } returns flowOf(config)

        val viewModel = LoginViewModel(configRepository, userRepository)

        // Act
        viewModel.switchMode(LoginMode.SYNDIC)
        viewModel.onSyndicPinChange(pin)
        viewModel.onLogin()

        // Assert
        assertEquals(true, viewModel.state.value.isAuthenticated)
        assertEquals(UserRole.SYNDIC, viewModel.state.value.authenticatedRole)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `syndic login failure`() = runTest {
        // Arrange
        val configRepository = mockk<ConfigRepository>()
        val userRepository = mockk<UserRepository>()
        val pin = "123456"
        val hashedPin = SecurityUtils.hashPin(pin)
        val config = ResidenceConfigEntity(
            residenceName = "Test Residence",
            masterPinHash = hashedPin,
            isSetupComplete = true
        )

        coEvery { configRepository.getConfig() } returns flowOf(config)

        val viewModel = LoginViewModel(configRepository, userRepository)

        // Act
        viewModel.switchMode(LoginMode.SYNDIC)
        viewModel.onSyndicPinChange("000000") // Wrong PIN
        viewModel.onLogin()

        // Assert
        assertEquals(false, viewModel.state.value.isAuthenticated)
        assertEquals(null, viewModel.state.value.authenticatedRole)
        assertEquals("Code Maître Incorrect", viewModel.state.value.error)
    }
}
