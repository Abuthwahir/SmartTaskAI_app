package com.smarttask.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smarttask.repository.TaskRepository
import com.smarttask.utils.AlarmScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * RecurringTaskWorker — runs daily via WorkManager to:
 *
 *  1. Generate today's instances of all daily/weekly recurring tasks (deduped by title).
 *  2. Re-schedule alarms for all pending future tasks (restores alarms after reboot).
 *
 * Uses @HiltWorker so Hilt can inject [TaskRepository] and [AlarmScheduler].
 * Scheduled in [SmartTaskApp.onCreate] as a unique periodic work request.
 */
@HiltWorker
class RecurringTaskWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RecurringTaskWorker"
        const val WORK_NAME = "recurring_task_worker"

        /**
         * Enqueue this worker from [SmartTaskApp] or wherever appropriate.
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so it is not re-enqueued if already scheduled.
         */
        fun enqueue(context: Context) {
            try {
                val request = PeriodicWorkRequestBuilder<RecurringTaskWorker>(
                    repeatInterval = 1,
                    repeatIntervalTimeUnit = TimeUnit.DAYS
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
                Log.d(TAG, "RecurringTaskWorker enqueued")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enqueue RecurringTaskWorker: ${e.message}")
            }
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() starting")
        return try {
            // Step 1: Create today's instances of recurring tasks
            repository.generateRecurringInstances()

            // Step 2: Re-schedule alarms for all pending future tasks
            // (covers the reboot case where AlarmManager loses all alarms)
            val pendingTasks = repository.getAllTasks().filter { task ->
                !task.completed && task.getEpochMillis() > System.currentTimeMillis()
            }
            alarmScheduler.rescheduleAllAlarms(pendingTasks)

            Log.d(TAG, "doWork() completed: ${pendingTasks.size} alarms rescheduled")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork() failed: ${e.message}")
            Result.retry()
        }
    }
}
