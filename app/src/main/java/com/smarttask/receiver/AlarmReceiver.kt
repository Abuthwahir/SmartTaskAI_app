package com.smarttask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smarttask.service.AlarmSoundService
import com.smarttask.ui.alarm.AlarmActivity
import com.smarttask.utils.AlarmScheduler
import com.smarttask.utils.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * AlarmReceiver — handles two broadcast actions:
 *
 *  1. [BOOT_COMPLETED] — device rebooted; WorkManager restores recurring alarms
 *     (the RecurringTaskWorker reschedules all pending alarms on its next run).
 *
 *  2. [ALARM_TRIGGER] — an AlarmManager alarm fired for a specific task.
 *     Starts AlarmSoundService (foreground) + shows AlarmActivity full-screen
 *     + posts a notification with Done/Snooze action buttons.
 *
 * NOTE: We do NOT use @AndroidEntryPoint here because this receiver can fire
 * during BOOT_COMPLETED before the Application is fully initialized by Hilt.
 * Instead we use [EntryPointAccessors] which is safe in all lifecycle states.
 */
class AlarmReceiver : BroadcastReceiver() {

    /**
     * Hilt entry point — declares which dependencies this receiver needs.
     * Resolved lazily via [EntryPointAccessors.fromApplication].
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun notificationHelper(): NotificationHelper
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_ALARM_TRIGGER = "com.smarttask.ALARM_TRIGGER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // WorkManager's RecurringTaskWorker will reschedule all alarms on next run.
                // No action needed here — WorkManager survives reboots automatically.
                Log.d(TAG, "Reboot detected — WorkManager will restore alarms")
            }

            ACTION_ALARM_TRIGGER -> {
                val taskId = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L)
                val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE)
                    ?: "Task Reminder"
                val taskDesc = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_DESCRIPTION)
                    ?: ""
                val taskPriority = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_PRIORITY)
                    ?: "medium"

                if (taskId == -1L) {
                    Log.e(TAG, "Invalid task ID in alarm intent — ignoring")
                    return
                }

                // 1. Start foreground service for alarm sound + vibration
                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
                    putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // 2. Launch full-screen alarm activity (works even on lock screen)
                val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
                    putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
                    putExtra(AlarmScheduler.EXTRA_TASK_DESCRIPTION, taskDesc)
                    putExtra(AlarmScheduler.EXTRA_TASK_PRIORITY, taskPriority)
                }
                context.startActivity(alarmIntent)

                // 3. Post a notification with Done/Snooze buttons via EntryPoint
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        AlarmReceiverEntryPoint::class.java
                    )
                    entryPoint.notificationHelper()
                        .showAlarmNotification(taskId, taskTitle, taskDesc)
                } catch (e: Exception) {
                    Log.e(TAG, "Could not show notification: ${e.message}")
                }
            }
        }
    }
}
