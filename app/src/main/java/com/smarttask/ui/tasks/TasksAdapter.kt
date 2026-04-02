package com.smarttask.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smarttask.R
import com.smarttask.database.entities.TaskEntity
import com.smarttask.database.entities.Priority
import com.smarttask.databinding.ItemTaskBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TasksAdapter(
    private val onToggleComplete: (TaskEntity) -> Unit,
    private val onEdit: (TaskEntity) -> Unit,
    private val onDelete: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TasksAdapter.TaskViewHolder>(DiffCallback) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            binding.apply {
                textTitle.text = task.title
                textTime.text = formatTime(task.time)
                textDate.text = formatDate(task.date)

                // Priority badge
                val priority = Priority.from(task.priority)
                val (bgColor, textColor) = when (priority) {
                    Priority.HIGH -> Pair(R.color.priority_high_bg, R.color.priority_high)
                    Priority.MEDIUM -> Pair(R.color.priority_medium_bg, R.color.priority_medium)
                    Priority.LOW -> Pair(R.color.priority_low_bg, R.color.priority_low)
                }
                chipPriority.apply {
                    text = priority.displayName
                    setChipBackgroundColorResource(bgColor)
                    setTextColor(ContextCompat.getColor(context, textColor))
                }

                // Category icon
                val iconRes = when (task.category) {
                    "work" -> R.drawable.ic_work
                    "health" -> R.drawable.ic_health
                    "study" -> R.drawable.ic_study
                    "personal" -> R.drawable.ic_personal
                    else -> R.drawable.ic_general
                }
                imageCategory.setImageResource(iconRes)

                // Recurring indicator
                iconRecurring.visibility = if (task.recurring != "none") View.VISIBLE else View.GONE

                // Completed state
                checkboxComplete.isChecked = task.completed
                if (task.completed) {
                    textTitle.paintFlags = textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textTitle.alpha = 0.5f
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textTitle.alpha = 1f
                }

                // Overdue indicator
                val overdue = task.isOverdue()
                if (overdue) {
                    textDate.setTextColor(ContextCompat.getColor(root.context, R.color.priority_high))
                    textDate.text = "⚠ Overdue · ${formatDate(task.date)}"
                } else {
                    textDate.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                }

                // Click listeners
                checkboxComplete.setOnClickListener { onToggleComplete(task) }
                root.setOnClickListener { onEdit(task) }
                buttonDelete.setOnClickListener { onDelete(task) }
            }
        }

        private fun formatTime(time: String): String {
            if (time.isBlank()) return ""
            return try {
                val (h, m) = time.split(":").map { it.toInt() }
                val ampm = if (h >= 12) "PM" else "AM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                "$h12:${m.toString().padStart(2, '0')} $ampm"
            } catch (e: Exception) { time }
        }

        private fun formatDate(date: String): String {
            return try {
                val ld = LocalDate.parse(date)
                val today = LocalDate.now()
                when {
                    ld == today -> "Today"
                    ld == today.plusDays(1) -> "Tomorrow"
                    else -> ld.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                }
            } catch (e: Exception) { date }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(old: TaskEntity, new: TaskEntity) = old.id == new.id
            override fun areContentsTheSame(old: TaskEntity, new: TaskEntity) = old == new
        }
    }
}
