package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getPendingTasks(): Flow<List<TaskEntity>>
    suspend fun createTask(title: String, dueDate: java.util.Date, isRecurring: Boolean): Result<Unit>
    suspend fun completeTask(taskId: String): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun generateMonthlyTasks() // Auto-generate recurring tasks
}
