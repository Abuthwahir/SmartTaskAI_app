package com.smarttask

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.smarttask.worker.RecurringTaskWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * SmartTaskApp — Application class.
 *
 * Responsibilities:
 *  1. Initialize Hilt DI (@HiltAndroidApp).
 *  2. Create all notification channels on Android O+.
 *  3. Enqueue the daily [RecurringTaskWorker] (generates recurring task instances
 *     and re-schedules all alarms — including after device reboot).
 *  4. Provide custom WorkManager configuration so Hilt can inject into workers.
 */
@HiltAndroidApp
class SmartTaskApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // Schedule the daily recurring-task + alarm-restore job
        // RecurringTaskWorker.enqueue(this)
    }

    // WorkManager must use the Hilt-aware factory so @HiltWorker injection works
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming task deadlines"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "Task Alarms",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Full-screen alarm alerts for tasks"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used while alarm sound is playing"
            }

            manager.createNotificationChannels(
                listOf(reminderChannel, alarmChannel, serviceChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "smarttask_reminders"
        const val CHANNEL_ALARMS    = "smarttask_alarms"
        const val CHANNEL_SERVICE   = "smarttask_service"
    }
}
