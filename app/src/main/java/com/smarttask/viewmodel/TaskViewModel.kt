package com.smarttask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smarttask.database.entities.TaskEntity
import com.smarttask.repository.TaskRepository
import com.smarttask.repository.TaskStats
import com.smarttask.utils.GeminiAIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * TaskViewModel — MVVM ViewModel for all task-related UI operations.
 * Observed by TasksFragment, CalendarFragment, AddEditTaskFragment, SettingsFragment.
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val geminiService: GeminiAIService
) : ViewModel() {

    // ── LiveData streams ───────────────────────────────────────────────────
    val allTasks: LiveData<List<TaskEntity>> = repository.getAllTasksFlow().asLiveData()
    val pendingTasks: LiveData<List<TaskEntity>> = repository.getPendingTasksFlow().asLiveData()
    val completedTasks: LiveData<List<TaskEntity>> = repository.getCompletedTasksFlow().asLiveData()

    private val _selectedDate = MutableLiveData(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    val selectedDate: LiveData<String> = _selectedDate

    private val _tasksByDate = MutableLiveData<List<TaskEntity>>()
    val tasksByDate: LiveData<List<TaskEntity>> = _tasksByDate

    private val _searchResults = MutableLiveData<List<TaskEntity>>()
    val searchResults: LiveData<List<TaskEntity>> = _searchResults

    private val _stats = MutableLiveData<TaskStats>()
    val stats: LiveData<TaskStats> = _stats

    private val _aiLoading = MutableLiveData(false)
    val aiLoading: LiveData<Boolean> = _aiLoading

    private val _aiParsedTask = MutableLiveData<GeminiAIService.ParsedTask?>()
    val aiParsedTask: LiveData<GeminiAIService.ParsedTask?> = _aiParsedTask

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    init {
        loadStats()
        loadTasksByDate()
    }

    // ── Calendar ───────────────────────────────────────────────────────────

    fun selectDate(date: String) {
        _selectedDate.value = date
        loadTasksByDate()
    }

    private fun loadTasksByDate() {
        viewModelScope.launch {
            val date = _selectedDate.value ?: return@launch
            _tasksByDate.value = repository.getTasksByDate(date)
        }
    }

    /**
     * Returns a Flow for a single task by ID.
     * AddEditTaskFragment observes this to pre-populate edit form.
     */
    fun getTaskByIdFlow(id: Long): Flow<TaskEntity?> = repository.getTaskByIdFlow(id)

    // ── CRUD ───────────────────────────────────────────────────────────────

    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.insertTask(task)
                _operationSuccess.value = "Task created!"
                loadStats()
            } catch (e: Exception) {
                _error.value = "Failed to save task: ${e.message}"
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
                _operationSuccess.value = "Task updated!"
                loadStats()
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTask(id)
                _operationSuccess.value = "Task deleted"
                loadStats()
            } catch (e: Exception) {
                _error.value = "Failed to delete task"
            }
        }
    }

    fun toggleComplete(id: Long) {
        viewModelScope.launch {
            repository.toggleComplete(id)
            loadStats()
        }
    }

    fun snoozeTask(id: Long, snoozeMinutes: Int = 10) {
        viewModelScope.launch {
            repository.snoozeTask(id, snoozeMinutes)
        }
    }

    fun searchTasks(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchTasks(query)
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = repository.getStats()
        }
    }

    // ── AI ─────────────────────────────────────────────────────────────────

    fun parseTaskWithAI(text: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            try {
                val parsed = geminiService.parseTaskFromText(text) // key resolved by GeminiAIService
                _aiParsedTask.value = parsed
            } catch (e: Exception) {
                _error.value = "AI parsing failed: ${e.message}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun prioritizeTasksWithAI() {
        viewModelScope.launch {
            _aiLoading.value = true
            try {
                val tasks = repository.getAllTasks()
                val scores = geminiService.prioritizeTasks(tasks) // key resolved by GeminiAIService
                scores.forEach { (id, score) -> repository.updateAiScore(id, score) }
                _operationSuccess.value = "Tasks re-prioritized by AI!"
            } catch (e: Exception) {
                _error.value = "AI prioritization failed: ${e.message}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun clearAiParsedTask() { _aiParsedTask.value = null }
    fun clearError()        { _error.value = null }
    fun clearSuccess()      { _operationSuccess.value = null }
}
