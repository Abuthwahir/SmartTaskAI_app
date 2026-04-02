package com.smarttask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smarttask.repository.TaskRepository
import com.smarttask.service.AlarmSoundService
import com.smarttask.utils.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * NotificationActionReceiver — handles taps on notification action buttons:
 *   ✅ Done    → marks the task complete, cancels alarm + notification
 *   😴 Snooze  → re-schedules alarm 10 min later
 *   ✖ Dismiss  → cancels alarm + notification, task stays pending
 *
 * NOTE: BroadcastReceivers must NOT use @AndroidEntryPoint because they can be
 * invoked before the Application is fully initialised by Hilt (e.g. on reboot).
 * We use the safer [EntryPointAccessors] pattern here.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationActionEntryPoint {
        fun taskRepository(): TaskRepository
        fun notificationHelper(): NotificationHelper
    }

    companion object {
        const val ACTION_DONE    = "com.smarttask.ACTION_DONE"
        const val ACTION_SNOOZE  = "com.smarttask.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.smarttask.ACTION_DISMISS"
        const val EXTRA_TASK_ID  = "task_id"
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) {
            Log.e(TAG, "Received action with invalid task ID — ignoring")
            return
        }

        Log.d(TAG, "Action: ${intent.action} for task $taskId")

        // Stop alarm sound service immediately (before any async work)
        context.stopService(Intent(context, AlarmSoundService::class.java))

        // Resolve dependencies safely
        val ep = try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                NotificationActionEntryPoint::class.java
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not resolve EntryPoint: ${e.message}")
            return
        }

        val repo = ep.taskRepository()
        val notifHelper = ep.notificationHelper()

        // Cancel the notification
        notifHelper.cancelNotification(taskId.toInt())

        when (intent.action) {
            ACTION_DONE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repo.toggleComplete(taskId)
                        Log.d(TAG, "Task $taskId marked complete via notification")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking complete: ${e.message}")
                    }
                }
            }
            ACTION_SNOOZE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repo.snoozeTask(taskId, snoozeMinutes = 10)
                        Log.d(TAG, "Task $taskId snoozed 10 min via notification")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error snoozing: ${e.message}")
                    }
                }
            }
            ACTION_DISMISS -> {
                // Alarm + notification already cancelled above
                Log.d(TAG, "Task $taskId alarm dismissed — task remains pending")
            }
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }
}
