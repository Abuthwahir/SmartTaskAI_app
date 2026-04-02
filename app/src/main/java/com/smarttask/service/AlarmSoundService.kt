package com.smarttask.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smarttask.R
import com.smarttask.SmartTaskApp
import com.smarttask.ui.alarm.AlarmActivity
import com.smarttask.utils.AlarmScheduler

class AlarmSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var taskId: Long = -1L
    private var taskTitle: String = "Task Reminder"

    companion object {
        private const val TAG = "AlarmSoundService"
        private const val NOTIFICATION_ID = 9999
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 300, 500, 300, 800)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L) ?: -1L
        taskTitle = intent?.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        playAlarm()
        startVibration()

        // Auto-stop after 60 seconds to avoid infinite alarm
        android.os.Handler(mainLooper).postDelayed({
            stopSelf()
        }, 60_000)

        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, taskId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SmartTaskApp.CHANNEL_SERVICE)
            .setContentTitle("⏰ $taskTitle")
            .setContentText("Alarm is ringing...")
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(fullScreenPendingIntent)
            .build()
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(this@AlarmSoundService, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm: ${e.message}")
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Vibrator::class.java)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(VIBRATION_PATTERN, 0) // 0 = repeat
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VIBRATION_PATTERN, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        Log.d(TAG, "AlarmSoundService destroyed")
    }
}
