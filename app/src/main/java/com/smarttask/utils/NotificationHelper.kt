package com.smarttask.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smarttask.R
import com.smarttask.SmartTaskApp
import com.smarttask.receiver.NotificationActionReceiver
import com.smarttask.ui.alarm.AlarmActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showAlarmNotification(taskId: Long, title: String, description: String) {
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(AlarmScheduler.EXTRA_TASK_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_TASK_DESCRIPTION, description)
        }
        val fullScreenPI = PendingIntent.getActivity(
            context, taskId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done action
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DONE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
        }
        val donePI = PendingIntent.getBroadcast(
            context, (taskId * 10 + 1).toInt(), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
        }
        val snoozePI = PendingIntent.getBroadcast(
            context, (taskId * 10 + 2).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SmartTaskApp.CHANNEL_ALARMS)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("⏰ $title")
            .setContentText(description.ifBlank { "Time to complete your task!" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPI, true)
            .addAction(R.drawable.ic_check, "Done", donePI)
            .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozePI)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    fun showReminderNotification(taskId: Long, title: String, description: String, minutesBefore: Int = 15) {
        val notification = NotificationCompat.Builder(context, SmartTaskApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📋 Reminder: $title")
            .setContentText(
                if (minutesBefore > 0) "Due in $minutesBefore minutes"
                else description.ifBlank { "Task due now!" }
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((taskId + 50000).toInt(), notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }
}
