package com.syndic.app.ui.community

import com.syndic.app.data.local.entity.IncidentEntity
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.ui.community.incident.IncidentViewModel
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
class IncidentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val communityRepository = mockk<CommunityRepository>()
    private val userRepository = mockk<UserRepository>()

    private fun createViewModel() = IncidentViewModel(communityRepository, userRepository)

    @Test
    fun `loadIncidents for Syndic shows all incidents`() = runTest {
        // Arrange
        val syndic = UserEntity("syndic", "email", "Syndic", "One", UserRole.SYNDIC, null, null, null, "A", "AP0", null, null, null)
        val incidents = listOf(
            IncidentEntity("1", "u1", "Leak", "Water", null, IncidentStatus.OPEN, com.syndic.app.data.local.entity.IncidentPriority.NORMAL, Date(), Date())
        )

        coEvery { userRepository.getCurrentUser() } returns flowOf(syndic)
        coEvery { communityRepository.getAllIncidents() } returns flowOf(incidents)

        // Act
        val viewModel = createViewModel()

        // Assert
        assertEquals(true, viewModel.uiState.value.isSyndic)
        assertEquals(1, viewModel.uiState.value.incidents.size)
    }

    @Test
    fun `loadIncidents for Resident shows only own incidents`() = runTest {
        // Arrange
        val resident = UserEntity("r1", "email", "Res", "One", UserRole.RESIDENT, null, null, null, "A", "AP1", null, null, null)
        val incidents = listOf(
            IncidentEntity("1", "r1", "Leak", "Water", null, IncidentStatus.OPEN, com.syndic.app.data.local.entity.IncidentPriority.NORMAL, Date(), Date())
        )

        coEvery { userRepository.getCurrentUser() } returns flowOf(resident)
        coEvery { communityRepository.getUserIncidents("r1") } returns flowOf(incidents)

        // Act
        val viewModel = createViewModel()

        // Assert
        assertEquals(false, viewModel.uiState.value.isSyndic)
        assertEquals(1, viewModel.uiState.value.incidents.size)
    }
}
