package com.smarttask.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "smarttask_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular SharedPreferences if encryption or KeyStore fails
        context.getSharedPreferences("smarttask_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_SNOOZE_DURATION = "snooze_duration"
        private const val KEY_DEFAULT_ALARM_TONE = "alarm_tone"
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()

    fun isAiEnabled(): Boolean = prefs.getBoolean(KEY_AI_ENABLED, true)
    fun setAiEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply()

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "") ?: ""
    fun setApiKey(key: String) = prefs.edit().putString(KEY_API_KEY, key).apply()

    fun getSnoozeDuration(): Int = prefs.getInt(KEY_SNOOZE_DURATION, 10)
    fun setSnoozeDuration(minutes: Int) = prefs.edit().putInt(KEY_SNOOZE_DURATION, minutes).apply()

    fun getAlarmTone(): String = prefs.getString(KEY_DEFAULT_ALARM_TONE, "default") ?: "default"
    fun setAlarmTone(tone: String) = prefs.edit().putString(KEY_DEFAULT_ALARM_TONE, tone).apply()
}
