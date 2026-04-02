package com.smarttask.database.dao

import androidx.room.*
import com.smarttask.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY date ASC, time ASC")
    fun getPendingTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC")
    fun getTasksByDateFlow(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC")
    suspend fun getTasksByDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC")
    suspend fun getTasksBetweenDates(startDate: String, endDate: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskByIdFlow(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE completed = 0 AND date <= :today ORDER BY ai_priority_score DESC, date ASC, time ASC")
    suspend fun getOverduePendingTasks(today: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE recurring != 'none' AND completed = 0")
    suspend fun getRecurringTasks(): List<TaskEntity>

    @Query("""
        SELECT * FROM tasks 
        WHERE completed = 0 
        AND date = :date 
        AND time BETWEEN :startTime AND :endTime
    """)
    suspend fun getTasksInTimeRange(date: String, startTime: String, endTime: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET completed = 1, completed_at = :completedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET completed = 0, completed_at = NULL, updated_at = :updatedAt WHERE id = :id")
    suspend fun markIncomplete(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET snoozed_until = :snoozedUntil WHERE id = :id")
    suspend fun snoozeTask(id: Long, snoozedUntil: Long)

    @Query("UPDATE tasks SET ai_priority_score = :score WHERE id = :id")
    suspend fun updateAiPriorityScore(id: Long, score: Float)

    @Query("UPDATE tasks SET ai_suggested_time = :suggestedTime WHERE id = :id")
    suspend fun updateAiSuggestedTime(id: Long, suggestedTime: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE priority = 'high' AND completed = 0")
    suspend fun getHighPriorityPendingCount(): Int

    @Query("SELECT * FROM tasks WHERE category = :category AND completed = 0 ORDER BY date ASC")
    suspend fun getTasksByCategory(category: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY date ASC")
    suspend fun searchTasks(query: String): List<TaskEntity>

    // Today's tasks for widget
    @Query("SELECT * FROM tasks WHERE date = :today AND completed = 0 ORDER BY time ASC LIMIT 5")
    suspend fun getTodaysTasks(today: String): List<TaskEntity>
}
