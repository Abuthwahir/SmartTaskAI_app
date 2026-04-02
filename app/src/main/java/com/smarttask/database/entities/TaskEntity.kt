package com.smarttask.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "date")
    val date: String,  // ISO format: YYYY-MM-DD

    @ColumnInfo(name = "time")
    val time: String,  // 24h format: HH:MM

    @ColumnInfo(name = "priority")
    val priority: String = "medium",  // high | medium | low

    @ColumnInfo(name = "category")
    val category: String = "general",  // general | work | health | study | personal

    @ColumnInfo(name = "recurring")
    val recurring: String = "none",  // none | daily | weekly | custom

    @ColumnInfo(name = "recurring_custom")
    val recurringCustom: String = "",  // e.g. "MON,WED,FRI"

    @ColumnInfo(name = "completed")
    val completed: Boolean = false,

    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Long? = null,  // epoch millis

    @ColumnInfo(name = "alarm_set")
    val alarmSet: Boolean = false,

    @ColumnInfo(name = "alarm_tone")
    val alarmTone: String = "default",

    @ColumnInfo(name = "ai_priority_score")
    val aiPriorityScore: Float = 0f,

    @ColumnInfo(name = "ai_suggested_time")
    val aiSuggestedTime: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
) {
    fun getEpochMillis(): Long {
        return try {
            val date = LocalDate.parse(date)
            val time = LocalTime.parse(time)
            val dateTime = date.atTime(time)
            dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    fun isOverdue(): Boolean {
        if (completed) return false
        return getEpochMillis() < System.currentTimeMillis()
    }
}

enum class Priority(val value: String, val displayName: String) {
    HIGH("high", "High"),
    MEDIUM("medium", "Medium"),
    LOW("low", "Low");

    companion object {
        fun from(value: String) = values().firstOrNull { it.value == value } ?: MEDIUM
    }
}

enum class Category(val value: String, val displayName: String, val icon: String) {
    GENERAL("general", "General", "circle"),
    WORK("work", "Work", "briefcase"),
    HEALTH("health", "Health", "heart"),
    STUDY("study", "Study", "book"),
    PERSONAL("personal", "Personal", "person");

    companion object {
        fun from(value: String) = values().firstOrNull { it.value == value } ?: GENERAL
    }
}

enum class Recurring(val value: String, val displayName: String) {
    NONE("none", "None"),
    DAILY("daily", "Daily"),
    WEEKLY("weekly", "Weekly"),
    CUSTOM("custom", "Custom");

    companion object {
        fun from(value: String) = values().firstOrNull { it.value == value } ?: NONE
    }
}
