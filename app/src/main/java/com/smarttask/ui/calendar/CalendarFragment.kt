package com.smarttask.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.smarttask.R
import com.smarttask.databinding.FragmentCalendarBinding
import com.smarttask.databinding.ItemCalendarDayBinding
import com.smarttask.ui.tasks.TasksAdapter
import com.smarttask.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val outerBinding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private var selectedDate: LocalDate = LocalDate.now()
    private lateinit var tasksAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return outerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupTasksList()
        setupAddButton()
        observeViewModel()

        viewModel.selectDate(selectedDate.format(DateTimeFormatter.ISO_DATE))
        updateSelectedDayHeader()
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        val daysOfWeek = java.time.DayOfWeek.values()
        outerBinding.layoutDayHeaders.children.forEachIndexed { index, headerView ->
            (headerView as TextView).text =
                daysOfWeek[(index + firstDayOfWeek.value - 1) % 7]
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }

        outerBinding.calendarView.apply {
            setup(startMonth, endMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)

            dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.bind(data)
                }
            }

            monthScrollListener = { month ->
                outerBinding.textMonthYear.text =
                    "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
            }
        }

        outerBinding.buttonPrevMonth.setOnClickListener {
            outerBinding.calendarView.findFirstVisibleMonth()?.let { month ->
                outerBinding.calendarView.smoothScrollToMonth(month.yearMonth.minusMonths(1))
            }
        }

        outerBinding.buttonNextMonth.setOnClickListener {
            outerBinding.calendarView.findFirstVisibleMonth()?.let { month ->
                outerBinding.calendarView.smoothScrollToMonth(month.yearMonth.plusMonths(1))
            }
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        private val binding = ItemCalendarDayBinding.bind(view)

        fun bind(day: CalendarDay) {
            binding.textDay.text = day.date.dayOfMonth.toString()

            if (day.position == DayPosition.MonthDate) {

                binding.textDay.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                )

                val isSelected = day.date == selectedDate
                val isToday = day.date == LocalDate.now()

                when {
                    isSelected -> {
                        binding.viewBackground.setBackgroundResource(R.drawable.bg_calendar_selected)
                        binding.textDay.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                    }
                    isToday -> {
                        binding.viewBackground.setBackgroundResource(R.drawable.bg_calendar_today)
                    }
                    else -> {
                        binding.viewBackground.background = null
                    }
                }

                val tasksForDay = viewModel.allTasks.value
                    ?.filter { it.date == day.date.format(DateTimeFormatter.ISO_DATE) }
                    ?: emptyList()

                binding.dotsContainer.removeAllViews()

                tasksForDay.take(3).forEach { task ->
                    val colorRes = when (task.priority) {
                        "high" -> R.color.priority_high
                        "medium" -> R.color.priority_medium
                        else -> R.color.priority_low
                    }

                    val dot = View(requireContext()).apply {
                        setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
                        layoutParams = ViewGroup.MarginLayoutParams(8, 8).also {
                            it.marginEnd = 2
                        }
                    }

                    binding.dotsContainer.addView(dot)
                }

                view.setOnClickListener {
                    val prev = selectedDate
                    selectedDate = day.date

                    viewModel.selectDate(day.date.format(DateTimeFormatter.ISO_DATE))

                    outerBinding.calendarView.notifyDateChanged(prev)
                    outerBinding.calendarView.notifyDateChanged(day.date)

                    updateSelectedDayHeader()
                }

            } else {
                binding.textDay.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_muted)
                )
                binding.viewBackground.background = null
                view.setOnClickListener(null)
            }
        }
    }

    private fun updateSelectedDayHeader() {
        outerBinding.textSelectedDate.text =
            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }

    private fun setupTasksList() {
        tasksAdapter = TasksAdapter(
            onToggleComplete = { task -> viewModel.toggleComplete(task.id) },
            onEdit = { task ->
                findNavController().navigate(
                    R.id.action_calendarFragment_to_addEditTaskFragment,
                    Bundle().apply { putLong("taskId", task.id) }
                )
            },
            onDelete = { task -> viewModel.deleteTask(task.id) }
        )

        outerBinding.recyclerViewDayTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }

    private fun setupAddButton() {
        outerBinding.fabAddTaskForDate.setOnClickListener {
            findNavController().navigate(
                R.id.action_calendarFragment_to_addEditTaskFragment,
                Bundle().apply {
                    putString("date", selectedDate.format(DateTimeFormatter.ISO_DATE))
                }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.tasksByDate.observe(viewLifecycleOwner) { tasks ->
            tasksAdapter.submitList(tasks)

            outerBinding.textNoTasks.visibility =
                if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.allTasks.observe(viewLifecycleOwner) {
            outerBinding.calendarView.notifyCalendarChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}