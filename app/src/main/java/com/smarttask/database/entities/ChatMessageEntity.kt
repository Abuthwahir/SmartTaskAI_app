package com.smarttask.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "role")
    val role: String,  // "user" | "assistant"

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String = "default",

    @ColumnInfo(name = "task_id")
    val taskId: Long? = null,  // if message created/referenced a task

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
