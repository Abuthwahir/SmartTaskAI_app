package com.smarttask.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smarttask.database.dao.ChatMessageDao
import com.smarttask.database.dao.TaskDao
import com.smarttask.database.entities.ChatMessageEntity
import com.smarttask.database.entities.TaskEntity

@Database(
    entities = [TaskEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "smarttask_db"
    }
}
