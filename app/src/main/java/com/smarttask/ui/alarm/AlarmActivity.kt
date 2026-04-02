package com.smarttask.ui.alarm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.smarttask.R
import com.smarttask.databinding.ActivityAlarmBinding
import com.smarttask.repository.TaskRepository
import com.smarttask.service.AlarmSoundService
import com.smarttask.utils.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * AlarmActivity — full-screen activity that fires when an alarm triggers.
 *
 * Shown over the lock screen (FLAG_SHOW_WHEN_LOCKED / FLAG_TURN_SCREEN_ON).
 * Provides three actions:
 *   • Snooze — re-schedules alarm 10 minutes later
 *   • Dismiss — stops the alarm without marking done
 *   • Done — marks the task complete and stops the alarm
 */
@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    @Inject
    lateinit var taskRepository: TaskRepository

    private var taskId: Long = -1L
    private var taskTitle: String = "Task Reminder"
    private var taskDescription: String = ""
    private var taskPriority: String = "medium"

    // Ticks the clock display every second
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, 1_000)
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show activity over the lock screen and turn on the display
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractIntentData()
        setupUI()
        setupButtons()
        startClock()
        startPulseAnimation()

        // Intercept back-press — treat it the same as "Dismiss"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.buttonDismiss.performClick()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacks(clockRunnable)
        binding.imageAlarmBell.clearAnimation()
    }

    // ── Data extraction ────────────────────────────────────────────────────

    private fun extractIntentData() {
        taskId          = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L)
        taskTitle       = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"
        taskDescription = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_DESCRIPTION) ?: ""
        taskPriority    = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_PRIORITY) ?: "medium"
    }

    // ── UI setup ───────────────────────────────────────────────────────────

    private fun setupUI() {
        binding.textTaskTitle.text = taskTitle
        binding.textTaskDescription.text =
            taskDescription.ifBlank { "Time to complete your task!" }

        // Color the priority bar at the top of the screen
        val priorityColor = when (taskPriority) {
            "high"   -> ContextCompat.getColor(this, R.color.priority_high)
            "medium" -> ContextCompat.getColor(this, R.color.priority_medium)
            else     -> ContextCompat.getColor(this, R.color.priority_low)
        }
        binding.viewPriorityBar.setBackgroundColor(priorityColor)

        // Priority chip
        binding.chipPriority.apply {
            text = taskPriority.replaceFirstChar { it.uppercase() }
            setChipBackgroundColorResource(
                when (taskPriority) {
                    "high"   -> R.color.priority_high_bg
                    "medium" -> R.color.priority_medium_bg
                    else     -> R.color.priority_low_bg
                }
            )
        }
    }

    private fun setupButtons() {
        // Snooze: re-schedule alarm in 10 minutes
        binding.buttonSnooze.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                taskRepository.snoozeTask(taskId, snoozeMinutes = 10)
            }
            stopAlarmAndFinish()
        }

        // Dismiss: just stop the sound, task remains pending
        binding.buttonDismiss.setOnClickListener {
            stopAlarmAndFinish()
        }

        // Done: mark task complete and stop alarm
        binding.buttonDone.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                taskRepository.toggleComplete(taskId)
            }
            stopAlarmAndFinish()
        }
    }

    // ── Clock ──────────────────────────────────────────────────────────────

    private fun startClock() {
        clockHandler.post(clockRunnable)
    }

    private fun updateClock() {
        binding.textClock.text  = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
        binding.textAmPm.text   = SimpleDateFormat("a",     Locale.getDefault()).format(Date())
    }

    // ── Animation ──────────────────────────────────────────────────────────

    private fun startPulseAnimation() {
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.imageAlarmBell.startAnimation(pulse)
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    private fun stopAlarmAndFinish() {
        // Stop foreground alarm sound service
        stopService(Intent(this, AlarmSoundService::class.java))
        // Cancel any pending notification for this task
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(taskId.toInt())
        finish()
    }
}
