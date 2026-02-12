package com.syndic.app.ui.matrix

import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.TransactionRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MatrixViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createResident(id: String, apartment: String): UserEntity {
        return UserEntity(
            id = id,
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            role = UserRole.RESIDENT,
            phoneNumber = null,
            cin = null,
            mandateStartDate = null,
            building = "A",
            apartmentNumber = apartment,
            pinHash = null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    @Test
    fun `when balance is strictly greater than 3 months fee, status is GOLD`() = runTest {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val transactionRepository = mockk<TransactionRepository>()
        val configRepository = mockk<ConfigRepository>()

        val monthlyFee = 100.0
        val balance = 301.0 // Strictly greater than 300

        coEvery { configRepository.getConfig() } returns flowOf(ResidenceConfigEntity(residenceName = "Test", monthlyFee = monthlyFee))
        coEvery { userRepository.getAllUsers() } returns listOf(createResident("1", "AP1"))
        coEvery { transactionRepository.getUserBalance("1") } returns flowOf(balance)

        val viewModel = MatrixViewModel(userRepository, transactionRepository, configRepository)

        // Act
        val state = viewModel.state.value

        // Assert
        assertEquals(MatrixColor.GOLD, state.residents[0].statusColor)
    }

    @Test
    fun `when balance is exactly 3 months fee, status is GREEN`() = runTest {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val transactionRepository = mockk<TransactionRepository>()
        val configRepository = mockk<ConfigRepository>()

        val monthlyFee = 100.0
        val balance = 300.0 // Exactly 3 months

        coEvery { configRepository.getConfig() } returns flowOf(ResidenceConfigEntity(residenceName = "Test", monthlyFee = monthlyFee))
        coEvery { userRepository.getAllUsers() } returns listOf(createResident("1", "AP1"))
        coEvery { transactionRepository.getUserBalance("1") } returns flowOf(balance)

        val viewModel = MatrixViewModel(userRepository, transactionRepository, configRepository)

        val state = viewModel.state.value

        // Assert
        assertEquals(MatrixColor.GREEN, state.residents[0].statusColor)
    }
}
