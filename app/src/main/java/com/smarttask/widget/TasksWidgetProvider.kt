package com.smarttask.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.smarttask.R
import com.smarttask.repository.TaskRepository
import com.smarttask.ui.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TasksWidgetProvider — home-screen widget showing today's top 3 pending tasks.
 *
 * NOTE: AppWidgetProvider extends BroadcastReceiver, so @AndroidEntryPoint is not
 * reliable. We use the [EntryPoint] + [EntryPointAccessors] pattern instead,
 * which safely resolves Hilt dependencies from the Application component.
 */
class TasksWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun taskRepository(): TaskRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Resolve TaskRepository safely via EntryPoint
                val repo = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    .taskRepository()

                val tasks = repo.getTodaysTasks()

                val taskText = if (tasks.isEmpty()) {
                    "No tasks today 🎉"
                } else {
                    tasks.take(3).joinToString("\n") { task ->
                        val timeStr = formatTime(task.time)
                        "• ${task.title}${if (timeStr.isNotEmpty()) " @ $timeStr" else ""}"
                    }
                }

                val openAppIntent = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val views = RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                    setTextViewText(R.id.widget_title, "Today's Tasks (${tasks.size})")
                    setTextViewText(R.id.widget_tasks_text, taskText)
                    setOnClickPendingIntent(R.id.widget_root, openAppIntent)
                }

                manager.updateAppWidget(widgetId, views)

            } catch (e: Exception) {
                // Widget update failed gracefully — show placeholder text
                val views = RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                    setTextViewText(R.id.widget_title, "SmartTask AI")
                    setTextViewText(R.id.widget_tasks_text, "Tap to open app")
                }
                manager.updateAppWidget(widgetId, views)
            }
        }
    }

    private fun formatTime(time: String): String {
        if (time.isBlank()) return ""
        return try {
            val (h, m) = time.split(":").map { it.toInt() }
            val ampm = if (h >= 12) "PM" else "AM"
            val h12 = if (h % 12 == 0) 12 else h % 12
            "$h12:${m.toString().padStart(2, '0')} $ampm"
        } catch (_: Exception) { time }
    }
}
