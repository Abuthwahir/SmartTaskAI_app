package com.smarttask.ui.add_edit

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.smarttask.R
import com.smarttask.database.entities.Category
import com.smarttask.database.entities.Priority
import com.smarttask.database.entities.Recurring
import com.smarttask.database.entities.TaskEntity
import com.smarttask.databinding.FragmentAddEditTaskBinding
import com.smarttask.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * AddEditTaskFragment — handles both creating a new task and editing an existing one.
 *
 * When [taskId] == -1L it creates a new task.
 * When [taskId] > 0 it pre-populates the form from the Room database.
 *
 * Features:
 *  - AI Natural Language parsing via Gemini
 *  - Voice (speech-to-text) input
 *  - DatePickerDialog / TimePickerDialog for date & time
 *  - Priority, Category, Recurring chip selectors
 */
@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel — scoped to the NavGraph / Activity
    private val viewModel: TaskViewModel by viewModels()

    // 0L means "add mode"; any positive value means "edit mode"
    private var taskId: Long = 0L
    private var existingTask: TaskEntity? = null

    // Form state — kept in fragment (not ViewModel) because it's purely transient UI state
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedHour: Int = 9
    private var selectedMinute: Int = 0
    private var selectedPriority: Priority = Priority.MEDIUM
    private var selectedCategory: Category = Category.GENERAL
    private var selectedRecurring: Recurring = Recurring.NONE

    // ── Activity Result Launchers ──────────────────────────────────────────

    /** Handles the speech-to-text result. */
    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull() ?: return@registerForActivityResult
        // Pre-fill AI input then auto-parse
        binding.editTextAiInput.setText(spokenText)
        viewModel.parseTaskWithAI(spokenText)
    }

    /** Requests RECORD_AUDIO permission before launching speech. */
    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startSpeechInput()
        else Snackbar.make(
            binding.root,
            "Microphone permission needed for voice input",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read taskId from fragment arguments (set by nav_graph <argument>)
        taskId = arguments?.getLong("taskId", 0L) ?: 0L

        setupPriorityChips()
        setupCategoryChips()
        setupRecurringChips()
        setupDateTimePickers()
        setupAiSection()
        setupVoiceInput()
        setupSaveButton()
        observeViewModel()

        if (taskId > 0L) {
            // Edit mode — load existing task from DB
            loadExistingTask()
            binding.textViewHeader.text = "Edit Task"
            binding.buttonSave.text = "Update Task"
        } else {
            // Add mode — pre-fill date from argument (calendar tap)
            val dateArg = arguments?.getString("date")
            if (!dateArg.isNullOrBlank()) {
                try {
                    selectedDate = LocalDate.parse(dateArg, DateTimeFormatter.ISO_DATE)
                } catch (_: Exception) {}
            }
            updateDateTimeDisplay()
        }
    }

    // ── Chip setup ─────────────────────────────────────────────────────────

    private fun setupPriorityChips() {
        Priority.values().forEach { priority ->
            Chip(requireContext()).apply {
                text = priority.displayName
                isCheckable = true
                isChecked = priority == selectedPriority
                setOnClickListener { selectedPriority = priority; syncPriorityChips() }
            }.also { binding.chipGroupPriority.addView(it) }
        }
    }

    private fun syncPriorityChips() {
        for (i in 0 until binding.chipGroupPriority.childCount) {
            val chip = binding.chipGroupPriority.getChildAt(i) as Chip
            chip.isChecked = (chip.text == selectedPriority.displayName)
        }
    }

    private fun setupCategoryChips() {
        Category.values().forEach { category ->
            Chip(requireContext()).apply {
                text = category.displayName
                isCheckable = true
                isChecked = category == selectedCategory
                setOnClickListener { selectedCategory = category; syncCategoryChips() }
            }.also { binding.chipGroupCategory.addView(it) }
        }
    }

    private fun syncCategoryChips() {
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val chip = binding.chipGroupCategory.getChildAt(i) as Chip
            chip.isChecked = (chip.text == selectedCategory.displayName)
        }
    }

    private fun setupRecurringChips() {
        Recurring.values().forEach { recurring ->
            Chip(requireContext()).apply {
                text = recurring.displayName
                isCheckable = true
                isChecked = recurring == selectedRecurring
                setOnClickListener { selectedRecurring = recurring; syncRecurringChips() }
            }.also { binding.chipGroupRecurring.addView(it) }
        }
    }

    private fun syncRecurringChips() {
        for (i in 0 until binding.chipGroupRecurring.childCount) {
            val chip = binding.chipGroupRecurring.getChildAt(i) as Chip
            chip.isChecked = (chip.text == selectedRecurring.displayName)
        }
    }

    // ── Date / Time pickers ────────────────────────────────────────────────

    private fun setupDateTimePickers() {
        binding.buttonDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    selectedDate = LocalDate.of(y, m + 1, d)
                    updateDateTimeDisplay()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }

        binding.buttonTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    updateDateTimeDisplay()
                },
                selectedHour,
                selectedMinute,
                false // 12-hour mode with AM/PM
            ).show()
        }
    }

    /** Refresh button labels to reflect current [selectedDate] and hour/minute. */
    private fun updateDateTimeDisplay() {
        binding.buttonDate.text =
            selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        val h12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
        val ampm = if (selectedHour >= 12) "PM" else "AM"
        binding.buttonTime.text = "$h12:${selectedMinute.toString().padStart(2, '0')} $ampm"
    }

    // ── AI section ─────────────────────────────────────────────────────────

    private fun setupAiSection() {
        binding.buttonAiParse.setOnClickListener {
            val text = binding.editTextAiInput.text?.toString()?.trim()
            if (text.isNullOrBlank()) {
                Snackbar.make(binding.root, "Enter a description first", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.parseTaskWithAI(text)
        }
    }

    private fun setupVoiceInput() {
        binding.buttonVoiceInput.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startSpeechInput()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your task...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Voice input not available on this device", Snackbar.LENGTH_SHORT).show()
        }
    }

    // ── Save / Cancel ──────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener { saveTask() }
        // Bottom bar Cancel button
        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        // Header back arrow (same behaviour)
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text?.toString()?.trim()
        if (title.isNullOrBlank()) {
            binding.editTextTitle.error = getString(R.string.error_no_title)
            return
        }

        val task = TaskEntity(
            // Keep the existing ID if editing so Room does an UPDATE not INSERT
            id = existingTask?.id ?: 0L,
            title = title,
            description = binding.editTextDescription.text?.toString()?.trim() ?: "",
            date = selectedDate.format(DateTimeFormatter.ISO_DATE),
            time = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
            priority = selectedPriority.value,
            category = selectedCategory.value,
            recurring = selectedRecurring.value,
            alarmSet = true,
            updatedAt = System.currentTimeMillis()
        )

        if (existingTask != null) viewModel.updateTask(task)
        else viewModel.insertTask(task)

        findNavController().popBackStack()
    }

    // ── Load existing task ─────────────────────────────────────────────────

    /**
     * Observe the task Flow from the DB and pre-populate the form.
     * Only the first emission is used (done via [takeWhile] guard on existingTask).
     */
    private fun loadExistingTask() {
        viewModel.getTaskByIdFlow(taskId).asLiveData().observe(viewLifecycleOwner) { task ->
            if (task == null || existingTask != null) return@observe // already loaded
            existingTask = task

            binding.editTextTitle.setText(task.title)
            binding.editTextDescription.setText(task.description)

            selectedDate = try {
                LocalDate.parse(task.date, DateTimeFormatter.ISO_DATE)
            } catch (_: Exception) { LocalDate.now() }

            val parts = task.time.split(":")
            selectedHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            selectedMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            selectedPriority = Priority.from(task.priority)
            selectedCategory = Category.from(task.category)
            selectedRecurring = Recurring.from(task.recurring)

            updateDateTimeDisplay()
            syncPriorityChips()
            syncCategoryChips()
            syncRecurringChips()
        }
    }

    // ── ViewModel observers ────────────────────────────────────────────────

    private fun observeViewModel() {
        // Show/hide AI loading spinner
        viewModel.aiLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarAi.visibility = if (loading) View.VISIBLE else View.GONE
            binding.buttonAiParse.isEnabled = !loading
        }

        // Apply AI-parsed task data to form fields
        viewModel.aiParsedTask.observe(viewLifecycleOwner) { parsed ->
            parsed ?: return@observe

            if (parsed.title.isNotBlank())
                binding.editTextTitle.setText(parsed.title)
            if (parsed.description.isNotBlank())
                binding.editTextDescription.setText(parsed.description)
            if (parsed.date.isNotBlank()) {
                selectedDate = try {
                    LocalDate.parse(parsed.date, DateTimeFormatter.ISO_DATE)
                } catch (_: Exception) { LocalDate.now() }
            }
            if (parsed.time.isNotBlank()) {
                val parts = parsed.time.split(":")
                selectedHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
                selectedMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            }

            selectedPriority = Priority.from(parsed.priority)
            selectedCategory = Category.from(parsed.category)
            selectedRecurring = Recurring.from(parsed.recurring)

            updateDateTimeDisplay()
            syncPriorityChips()
            syncCategoryChips()
            syncRecurringChips()

            binding.editTextAiInput.setText("")
            Snackbar.make(binding.root, "✨ AI filled in the details!", Snackbar.LENGTH_SHORT).show()
            viewModel.clearAiParsedTask()
        }

        // Surface errors to user
        viewModel.error.observe(viewLifecycleOwner) { err ->
            err ?: return@observe
            Snackbar.make(binding.root, err, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
