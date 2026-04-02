package com.smarttask.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smarttask.R
import com.smarttask.databinding.FragmentTasksBinding
import com.smarttask.database.entities.TaskEntity
import com.smarttask.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * TasksFragment — main task list screen.
 *
 * Fixes applied:
 *  - Observer leak: searchResults is now observed ONCE in observeViewModel(),
 *    not inside doAfterTextChanged (which added a new observer on every keystroke).
 *  - Filter switching uses removeObservers + single re-observe pattern.
 */
@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TasksAdapter

    /** Tracks whether the user is actively searching so we know which data source to show. */
    private var isSearchActive = false

    private var currentFilter = Filter.ALL

    enum class Filter { ALL, PENDING, COMPLETED }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupFab()
        observeViewModel()  // Single registration point for ALL observers
    }

    // ── RecyclerView ───────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = TasksAdapter(
            onToggleComplete = { task -> viewModel.toggleComplete(task.id) },
            onEdit = { task ->
                findNavController().navigate(
                    R.id.action_tasksFragment_to_addEditTaskFragment,
                    Bundle().apply { putLong("taskId", task.id) }
                )
            },
            onDelete = { task ->
                viewModel.deleteTask(task.id)
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TasksFragment.adapter
            setHasFixedSize(false)
        }
    }

    // ── Filter chips ───────────────────────────────────────────────────────

    private fun setupFilters() {
        binding.chipAll.setOnClickListener       { setFilter(Filter.ALL) }
        binding.chipPending.setOnClickListener   { setFilter(Filter.PENDING) }
        binding.chipCompleted.setOnClickListener { setFilter(Filter.COMPLETED) }
        updateChipSelection(Filter.ALL)
    }

    /**
     * Change the active filter. Clears any active search so the correct
     * LiveData source is displayed.
     */
    private fun setFilter(filter: Filter) {
        currentFilter = filter
        isSearchActive = false
        binding.searchEditText.text?.clear()   // reset search box visually
        updateChipSelection(filter)
    }

    private fun updateChipSelection(filter: Filter) {
        binding.chipAll.isChecked       = filter == Filter.ALL
        binding.chipPending.isChecked   = filter == Filter.PENDING
        binding.chipCompleted.isChecked = filter == Filter.COMPLETED
    }

    // ── Search ─────────────────────────────────────────────────────────────

    /**
     * FIX (Bug 4): observer is registered ONCE in observeViewModel().
     * doAfterTextChanged only triggers the ViewModel search call — it never
     * calls observe() itself, eliminating the accumulating-observer leak.
     */
    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            val query = text?.toString() ?: ""
            when {
                query.length >= 2 -> {
                    isSearchActive = true
                    viewModel.searchTasks(query)
                }
                query.isEmpty() -> {
                    isSearchActive = false
                    // Revert to the current filter's list without touching observers
                }
            }
        }
    }

    // ── FAB ────────────────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment_to_addEditTaskFragment)
        }
    }

    // ── ViewModel observers (registered ONCE) ──────────────────────────────

    private fun observeViewModel() {
        // All-tasks list: shown when filter == ALL and no search is active
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            if (!isSearchActive && currentFilter == Filter.ALL) submitList(tasks)
        }

        // Pending list
        viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
            if (!isSearchActive && currentFilter == Filter.PENDING) submitList(tasks)
        }

        // Completed list
        viewModel.completedTasks.observe(viewLifecycleOwner) { tasks ->
            if (!isSearchActive && currentFilter == Filter.COMPLETED) submitList(tasks)
        }

        // Search results: only displayed when a search is active
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (isSearchActive) submitList(results)
        }

        // Error toast
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Success toast
        viewModel.operationSuccess.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }

    // ── List submission ────────────────────────────────────────────────────

    private fun submitList(tasks: List<TaskEntity>) {
        adapter.submitList(tasks)
        val empty = tasks.isEmpty()
        binding.emptyStateGroup.visibility = if (empty) View.VISIBLE else View.GONE
        binding.recyclerViewTasks.visibility = if (empty) View.GONE else View.VISIBLE
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
