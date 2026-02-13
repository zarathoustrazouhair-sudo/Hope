package com.syndic.app.ui.cockpit

import com.syndic.app.data.local.entity.TaskEntity
import com.syndic.app.domain.repository.TaskRepository
import com.syndic.app.ui.cockpit.tasks.TaskViewModel
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
class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val taskRepository = mockk<TaskRepository>()

    @Test
    fun `viewModel loads pending tasks`() = runTest {
        // Arrange
        val tasks = listOf(TaskEntity("1", "Task 1", null, Date()))
        coEvery { taskRepository.getPendingTasks() } returns flowOf(tasks)

        // Act
        val viewModel = TaskViewModel(taskRepository)

        // Assert
        assertEquals(1, viewModel.tasks.value.size)
        assertEquals("Task 1", viewModel.tasks.value[0].title)
    }
}
