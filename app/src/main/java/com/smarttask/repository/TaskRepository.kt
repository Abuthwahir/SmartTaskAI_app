package com.smarttask.repository

import com.smarttask.database.dao.ChatMessageDao
import com.smarttask.database.dao.TaskDao
import com.smarttask.database.entities.ChatMessageEntity
import com.smarttask.database.entities.TaskEntity
import com.smarttask.utils.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val chatMessageDao: ChatMessageDao,
    private val alarmScheduler: AlarmScheduler
) {

    // ── Tasks ──────────────────────────────────────────────────────────────

    fun getAllTasksFlow(): Flow<List<TaskEntity>> = taskDao.getAllTasksFlow()
    fun getPendingTasksFlow(): Flow<List<TaskEntity>> = taskDao.getPendingTasksFlow()
    fun getCompletedTasksFlow(): Flow<List<TaskEntity>> = taskDao.getCompletedTasksFlow()
    fun getTasksByDateFlow(date: String): Flow<List<TaskEntity>> = taskDao.getTasksByDateFlow(date)
    fun getTaskByIdFlow(id: Long): Flow<TaskEntity?> = taskDao.getTaskByIdFlow(id)

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun getAllTasks(): List<TaskEntity> = taskDao.getAllTasks()
    suspend fun getTasksByDate(date: String): List<TaskEntity> = taskDao.getTasksByDate(date)
    suspend fun searchTasks(query: String): List<TaskEntity> = taskDao.searchTasks(query)

    suspend fun insertTask(task: TaskEntity): Long {
        val id = taskDao.insertTask(task)
        val inserted = task.copy(id = id)
        alarmScheduler.scheduleAlarm(inserted)
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        if (!task.completed) {
            alarmScheduler.scheduleAlarm(task)
        } else {
            alarmScheduler.cancelAlarm(task.id)
        }
    }

    suspend fun deleteTask(id: Long) {
        alarmScheduler.cancelAlarm(id)
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleComplete(id: Long) {
        val task = taskDao.getTaskById(id) ?: return
        if (task.completed) {
            taskDao.markIncomplete(id)
            alarmScheduler.scheduleAlarm(task.copy(completed = false))
        } else {
            taskDao.markCompleted(id)
            alarmScheduler.cancelAlarm(id)
        }
    }

    suspend fun snoozeTask(id: Long, snoozeMinutes: Int = 10) {
        val task = taskDao.getTaskById(id) ?: return
        val snoozedUntil = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        taskDao.snoozeTask(id, snoozedUntil)
        alarmScheduler.scheduleSnooze(task, snoozeMinutes)
    }

    suspend fun updateAiScore(id: Long, score: Float) = taskDao.updateAiPriorityScore(id, score)

    // ── Statistics ─────────────────────────────────────────────────────────

    suspend fun getStats(): TaskStats {
        val total = taskDao.getTotalCount()
        val completed = taskDao.getCompletedCount()
        val pending = taskDao.getPendingCount()
        val highPriority = taskDao.getHighPriorityPendingCount()
        val completionRate = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0

        return TaskStats(
            total = total,
            completed = completed,
            pending = pending,
            highPriority = highPriority,
            completionRate = completionRate
        )
    }

    // ── Recurring Tasks ────────────────────────────────────────────────────

    suspend fun generateRecurringInstances() {
        val recurringTasks = taskDao.getRecurringTasks()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE

        recurringTasks.forEach { task ->
            val taskDate = try { LocalDate.parse(task.date, formatter) } catch (e: Exception) { return@forEach }

            when (task.recurring) {
                "daily" -> {
                    // Create instance for today if not exists
                    val todayStr = today.format(formatter)
                    val existing = taskDao.getTasksByDate(todayStr)
                    if (existing.none { it.title == task.title }) {
                        taskDao.insertTask(task.copy(
                            id = 0,
                            date = todayStr,
                            completed = false,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        ))
                    }
                }
                "weekly" -> {
                    // Create instance for this week's same day
                    val dayOfWeek = taskDate.dayOfWeek
                    val thisWeek = today.with(dayOfWeek)
                    if (thisWeek.isAfter(today.minusDays(1))) {
                        val weekStr = thisWeek.format(formatter)
                        val existing = taskDao.getTasksByDate(weekStr)
                        if (existing.none { it.title == task.title }) {
                            taskDao.insertTask(task.copy(
                                id = 0,
                                date = weekStr,
                                completed = false,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            ))
                        }
                    }
                }
            }
        }
    }

    // ── Chat History ───────────────────────────────────────────────────────

    fun getChatMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForSession(sessionId)

    suspend fun getChatMessagesOnce(sessionId: String): List<ChatMessageEntity> =
        chatMessageDao.getMessagesForSessionOnce(sessionId)

    suspend fun insertChatMessage(message: ChatMessageEntity): Long =
        chatMessageDao.insertMessage(message)

    suspend fun clearChatSession(sessionId: String) =
        chatMessageDao.clearSession(sessionId)

    // ── Today's tasks for widget ───────────────────────────────────────────
    suspend fun getTodaysTasks(): List<TaskEntity> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return taskDao.getTodaysTasks(today)
    }
}

data class TaskStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val highPriority: Int,
    val completionRate: Int
)
