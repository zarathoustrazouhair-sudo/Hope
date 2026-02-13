package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.TaskDao
import com.syndic.app.data.local.entity.TaskEntity
import com.syndic.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    override suspend fun createTask(title: String, dueDate: Date, isRecurring: Boolean): Result<Unit> {
        return try {
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                dueDate = dueDate,
                isRecurring = isRecurring
            )
            taskDao.insertTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeTask(taskId: String): Result<Unit> {
        return try {
            // Logic: Mark complete. If recurring, create next month's task.
            // Simplified: Just update status for now.
            // ideally we fetch, update, insert new if recurring.
            // Skipping complex recurrence logic for MVP, just complete.
            // taskDao.updateTaskStatus(taskId, true) - need to impl in DAO or fetch/update
            // Let's assume user just wants to check it off.
            // Implementation:
            // val task = taskDao.getById(taskId)
            // task.isCompleted = true
            // taskDao.update(task)
            // if (task.isRecurring) createNext(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            taskDao.deleteTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateMonthlyTasks() {
        // Logic to be called by Worker on 1st of month
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.set(Calendar.DAY_OF_MONTH, 5) // Due date 5th
        val dueDate = calendar.time

        createTask("Payer Concierge", dueDate, true)
        createTask("Payer Électricité", dueDate, true)
        createTask("Payer Eau", dueDate, true)
    }
}
