package com.smarttask.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smarttask.database.entities.TaskEntity
import com.smarttask.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmScheduler — schedules, cancels, and snoozes exact AlarmManager alarms.
 *
 * On Android 12+ uses [AlarmManager.setExactAndAllowWhileIdle] when the user
 * has granted SCHEDULE_EXACT_ALARM; falls back to [setAndAllowWhileIdle] otherwise.
 *
 * Each alarm is a broadcast to [AlarmReceiver] carrying task metadata as extras.
 * Snooze alarms use a separate PendingIntent request code (taskId + 10000) to
 * avoid overwriting the original alarm.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val SNOOZE_REQUEST_OFFSET = 10_000

        const val EXTRA_TASK_ID          = "task_id"
        const val EXTRA_TASK_TITLE       = "task_title"
        const val EXTRA_TASK_DESCRIPTION = "task_description"
        const val EXTRA_TASK_PRIORITY    = "task_priority"
        const val EXTRA_TASK_CATEGORY    = "task_category"
    }

    // ── Schedule ────────────────────────────────────────────────────────────

    /**
     * Schedule an exact alarm for [task].
     * Silently skips tasks whose trigger time is already in the past.
     */
    fun scheduleAlarm(task: TaskEntity) {
        val triggerMs = task.getEpochMillis()
        if (triggerMs <= System.currentTimeMillis()) {
            Log.w(TAG, "Task ${task.id} is in the past — skipping alarm")
            return
        }
        setAlarm(task.id.toInt(), triggerMs, buildIntent(task))
        Log.d(TAG, "Alarm set for task ${task.id} at $triggerMs")
    }

    /**
     * Re-schedule alarms for a list of tasks (e.g. after device reboot).
     * Only schedules tasks that are pending and in the future.
     */
    fun rescheduleAllAlarms(tasks: List<TaskEntity>) {
        tasks.forEach { task ->
            if (!task.completed && task.getEpochMillis() > System.currentTimeMillis()) {
                scheduleAlarm(task)
            }
        }
        Log.d(TAG, "Rescheduled ${tasks.size} alarms")
    }

    // ── Snooze ──────────────────────────────────────────────────────────────

    /**
     * Schedule a snooze alarm [snoozeMinutes] from now.
     * Uses a different request code so it doesn't cancel the original alarm.
     */
    fun scheduleSnooze(task: TaskEntity, snoozeMinutes: Int = 10) {
        val snoozeMs = System.currentTimeMillis() + (snoozeMinutes * 60_000L)
        val requestCode = task.id.toInt() + SNOOZE_REQUEST_OFFSET
        setAlarm(requestCode, snoozeMs, buildIntent(task))
        Log.d(TAG, "Snooze alarm for task ${task.id} in $snoozeMinutes min")
    }

    // ── Cancel ──────────────────────────────────────────────────────────────

    /**
     * Cancel both the original alarm and any pending snooze for [taskId].
     */
    fun cancelAlarm(taskId: Long) {
        cancelByRequestCode(taskId.toInt())
        cancelByRequestCode(taskId.toInt() + SNOOZE_REQUEST_OFFSET)
        Log.d(TAG, "Alarm cancelled for task $taskId")
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private fun buildIntent(task: TaskEntity): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(EXTRA_TASK_ID,          task.id)
            putExtra(EXTRA_TASK_TITLE,       task.title)
            putExtra(EXTRA_TASK_DESCRIPTION, task.description)
            putExtra(EXTRA_TASK_PRIORITY,    task.priority)
            putExtra(EXTRA_TASK_CATEGORY,    task.category)
        }

    private fun setAlarm(requestCode: Int, triggerMs: Long, intent: Intent) {
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            // Final fallback — inexact but still fires
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pi)
        }
    }

    private fun cancelByRequestCode(requestCode: Int) {
        val pi = PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
