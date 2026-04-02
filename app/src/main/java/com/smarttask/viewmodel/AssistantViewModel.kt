package com.smarttask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smarttask.database.entities.ChatMessageEntity
import com.smarttask.database.entities.TaskEntity
import com.smarttask.repository.TaskRepository
import com.smarttask.utils.GeminiAIService
import com.smarttask.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * AssistantViewModel — drives the AI chat screen.
 *
 * Fixes applied:
 *  - Now injects [PreferencesManager] so the runtime API key (set in Settings)
 *    is used instead of only the compile-time BuildConfig value.
 *  - AI toggle (isAiEnabled) is checked before sending any message.
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val geminiService: GeminiAIService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val SESSION_ID = "default"

    val messages: LiveData<List<ChatMessageEntity>> =
        repository.getChatMessages(SESSION_ID).asLiveData()

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _createdTask = MutableLiveData<TaskEntity?>()
    val createdTask: LiveData<TaskEntity?> = _createdTask

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            // Save user message to DB first (shows immediately in chat)
            repository.insertChatMessage(
                ChatMessageEntity(role = "user", content = userText, sessionId = SESSION_ID)
            )

            _isTyping.value = true

            // Check AI toggle — if disabled, reply with guidance message
            if (!preferencesManager.isAiEnabled()) {
                repository.insertChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = "AI features are disabled. Enable them in Settings → AI Features.",
                        sessionId = SESSION_ID
                    )
                )
                _isTyping.value = false
                return@launch
            }

            // Check API key availability
            val apiKey = preferencesManager.getApiKey().ifBlank {
                try { com.smarttask.BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
            }
            if (apiKey.isBlank()) {
                repository.insertChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = "No API key found. Go to Settings → AI Features → Enter your Gemini API key.",
                        sessionId = SESSION_ID
                    )
                )
                _isTyping.value = false
                return@launch
            }

            try {
                val history = repository.getChatMessagesOnce(SESSION_ID)
                    .takeLast(10)
                    .map { it.role to it.content }

                val currentTasks = repository.getAllTasks()

                val response = geminiService.chat(
                    userMessage = userText,
                    conversationHistory = history,
                    tasks = currentTasks,
                    apiKey = apiKey          // pass resolved runtime key
                )

                // Auto-create task if AI detected one
                var newTaskId: Long? = null
                if (response.parsedTask != null) {
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val newTask = TaskEntity(
                        title = response.parsedTask.title.ifBlank { "New Task" },
                        description = response.parsedTask.description,
                        date = response.parsedTask.date.ifBlank { today },
                        time = response.parsedTask.time.ifBlank { "09:00" },
                        priority = response.parsedTask.priority,
                        category = response.parsedTask.category,
                        recurring = response.parsedTask.recurring,
                        recurringCustom = response.parsedTask.recurring_custom,
                        alarmSet = true
                    )
                    newTaskId = repository.insertTask(newTask)
                    _createdTask.value = newTask.copy(id = newTaskId)
                }

                repository.insertChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = response.message,
                        sessionId = SESSION_ID,
                        taskId = newTaskId
                    )
                )

            } catch (e: Exception) {
                repository.insertChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = "Sorry, I couldn't connect. Check your internet connection and API key.",
                        sessionId = SESSION_ID
                    )
                )
                _error.value = e.message
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { repository.clearChatSession(SESSION_ID) }
    }

    fun clearCreatedTask() { _createdTask.value = null }
    fun clearError()        { _error.value = null }
}
