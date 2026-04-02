package com.smarttask.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.smarttask.database.entities.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GeminiAIService — wraps all calls to the Gemini 1.5 Flash API.
 *
 * Fixes applied:
 *  - Model upgraded: gemini-pro → gemini-1.5-flash (stable, free-tier friendly)
 *  - API key lookup: prefers runtime key from PreferencesManager,
 *    falls back to BuildConfig compile-time key.
 *  - AI toggle: all public functions check [isAiEnabled] before making network calls.
 */
@Singleton
class GeminiAIService @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val preferencesManager: PreferencesManager   // For runtime API key + AI toggle
) {
    companion object {
        private const val TAG = "GeminiAIService"
        // Updated: gemini-1.5-flash is stable, fast, and free-tier available
        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    }

    // ── Key resolution ─────────────────────────────────────────────────────

    /**
     * Returns the active API key.
     * Runtime key (set via Settings) always wins; falls back to BuildConfig value.
     */
    private fun resolveApiKey(explicitKey: String = ""): String {
        if (explicitKey.isNotBlank()) return explicitKey
        val runtimeKey = preferencesManager.getApiKey()
        if (runtimeKey.isNotBlank()) return runtimeKey
        return try {
            com.smarttask.BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) { "" }
    }

    /** Returns true when the user has AI features enabled in Settings. */
    private fun isEnabled(): Boolean = preferencesManager.isAiEnabled()

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Parse a natural-language string into structured task fields.
     * Returns null if AI is disabled or the parse fails.
     *
     * Example: "Remind me to take medicine every day at 8 AM"
     * → ParsedTask(title="Take medicine", time="08:00", recurring="daily", category="health")
     */
    suspend fun parseTaskFromText(text: String, apiKey: String = ""): ParsedTask? {
        if (!isEnabled()) {
            Log.d(TAG, "AI disabled — skipping NL parse")
            return null
        }
        val key = resolveApiKey(apiKey)
        if (key.isBlank()) return null

        return withContext(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val prompt = """
                Extract task details from this natural language text and return ONLY valid JSON (no markdown, no code blocks):
                Text: "$text"
                Today's date: $today

                Return JSON with exactly these fields:
                {
                  "title": "task title (required, concise)",
                  "description": "extra detail or empty string",
                  "date": "YYYY-MM-DD (today if not specified)",
                  "time": "HH:MM in 24h (09:00 if not specified)",
                  "priority": "high|medium|low",
                  "category": "work|health|study|personal|general",
                  "recurring": "none|daily|weekly",
                  "recurring_custom": ""
                }

                Context clues: medicine/doctor/gym → health; meeting/deadline/project → work;
                study/exam/homework → study; urgent/asap/important → high priority.
            """.trimIndent()

            try {
                val raw = callGemini(prompt, key)
                val clean = raw.replace("```json", "").replace("```", "").trim()
                gson.fromJson(clean, ParsedTask::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Parse error: ${e.message}")
                null
            }
        }
    }

    /**
     * Chat with the AI assistant, passing the last few conversation turns for context.
     * Returns a [AIChatResponse] — always non-null even on failure (safe fallback message).
     */
    suspend fun chat(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>,
        tasks: List<TaskEntity>,
        apiKey: String = ""
    ): AIChatResponse {
        if (!isEnabled()) {
            return AIChatResponse(
                "AI features are currently disabled. Enable them in Settings → AI Features.",
                null
            )
        }
        val key = resolveApiKey(apiKey)
        if (key.isBlank()) {
            return AIChatResponse(
                "No Gemini API key found. Please add one in Settings → AI Features → API Key.",
                null
            )
        }

        return withContext(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val pendingTasks = tasks.filter { !it.completed }.take(10)
            val tasksSummary = if (pendingTasks.isNotEmpty()) {
                "Current pending tasks:\n" + pendingTasks.joinToString("\n") {
                    "- ${it.title} (${it.priority} priority, due ${it.date} ${it.time})"
                }
            } else "No pending tasks."

            val history = conversationHistory.takeLast(6).joinToString("\n") {
                "${it.first}: ${it.second}"
            }

            val prompt = """
                You are SmartTask AI — a friendly, concise productivity assistant.
                Today: $today

                $tasksSummary
                ${if (history.isNotEmpty()) "\nConversation:\n$history\n" else ""}
                User: $userMessage

                If the user wants to create a task, extract it.
                Respond ONLY with valid JSON (no markdown):
                {"response": "your reply", "task_json": {"title":"...","date":"...","time":"...","priority":"...","category":"...","recurring":"none","description":"..."} or null}
            """.trimIndent()

            try {
                val raw = callGemini(prompt, key)
                val clean = raw.replace("```json", "").replace("```", "").trim()
                val obj = gson.fromJson(clean, JsonObject::class.java)
                val responseText = obj.get("response")?.asString ?: raw
                val taskJson = obj.get("task_json")
                val parsedTask = if (taskJson != null && !taskJson.isJsonNull) {
                    try { gson.fromJson(taskJson, ParsedTask::class.java) } catch (_: Exception) { null }
                } else null
                AIChatResponse(responseText, parsedTask)
            } catch (e: Exception) {
                Log.e(TAG, "Chat error: ${e.message}")
                AIChatResponse(
                    "I'm having trouble connecting. Please check your internet connection and API key.",
                    null
                )
            }
        }
    }

    /**
     * Score tasks 0.0–1.0 by urgency+importance and return a map of taskId → score.
     * Returns empty map if AI is disabled or fails.
     */
    suspend fun prioritizeTasks(tasks: List<TaskEntity>, apiKey: String = ""): Map<Long, Float> {
        if (!isEnabled() || tasks.isEmpty()) return emptyMap()
        val key = resolveApiKey(apiKey)
        if (key.isBlank()) return emptyMap()

        return withContext(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val taskList = tasks.take(20).joinToString("\n") {
                "${it.id}: ${it.title} | ${it.priority} | due ${it.date} | ${it.category}"
            }
            val prompt = """
                Score these tasks 0.0-1.0 by urgency (1.0 = most urgent). Today: $today
                Return ONLY JSON: {"scores": {"id": score, ...}}
                Tasks:
                $taskList
            """.trimIndent()

            try {
                val raw = callGemini(prompt, key)
                val clean = raw.replace("```json", "").replace("```", "").trim()
                val obj = gson.fromJson(clean, JsonObject::class.java)
                obj.getAsJsonObject("scores").entrySet().associate { (k, v) ->
                    k.toLong() to v.asFloat
                }
            } catch (e: Exception) {
                Log.e(TAG, "Prioritize error: ${e.message}")
                emptyMap()
            }
        }
    }

    // ── Private HTTP ───────────────────────────────────────────────────────

    private suspend fun callGemini(prompt: String, apiKey: String): String =
        withContext(Dispatchers.IO) {
            val body = gson.toJson(
                mapOf(
                    "contents" to listOf(
                        mapOf("parts" to listOf(mapOf("text" to prompt)))
                    ),
                    "generationConfig" to mapOf(
                        "temperature" to 0.3,
                        "maxOutputTokens" to 1024
                    )
                )
            )

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response")

            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: $responseBody")
            }

            gson.fromJson(responseBody, JsonObject::class.java)
                .getAsJsonArray("candidates")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString
                ?: throw Exception("Could not parse Gemini response")
        }

    // ── Data classes ───────────────────────────────────────────────────────

    data class ParsedTask(
        val title: String = "",
        val description: String = "",
        val date: String = "",
        val time: String = "09:00",
        val priority: String = "medium",
        val category: String = "general",
        val recurring: String = "none",
        val recurring_custom: String = ""
    )

    data class AIChatResponse(
        val message: String,
        val parsedTask: ParsedTask?
    )
}
