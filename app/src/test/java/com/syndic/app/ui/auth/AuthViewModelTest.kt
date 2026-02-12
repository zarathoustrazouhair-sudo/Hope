package com.syndic.app.ui.auth

import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is not authenticated`() = runTest {
        val userRepository = mockk<UserRepository>()
        val viewModel = AuthViewModel(userRepository)

        assertEquals(false, viewModel.uiState.value.isAuthenticated)
        assertEquals(null, viewModel.uiState.value.userRole)
    }

    @Test
    fun `onLocalLoginSuccess updates state`() = runTest {
        val userRepository = mockk<UserRepository>()
        val viewModel = AuthViewModel(userRepository)

        viewModel.onLocalLoginSuccess(UserRole.SYNDIC)

        assertEquals(true, viewModel.uiState.value.isAuthenticated)
        assertEquals(UserRole.SYNDIC, viewModel.uiState.value.userRole)
    }

    @Test
    fun `onLogout resets state`() = runTest {
        val userRepository = mockk<UserRepository>()
        val viewModel = AuthViewModel(userRepository)

        viewModel.onLocalLoginSuccess(UserRole.SYNDIC)
        viewModel.onLogout()

        assertEquals(false, viewModel.uiState.value.isAuthenticated)
        assertEquals(null, viewModel.uiState.value.userRole)
    }
}
