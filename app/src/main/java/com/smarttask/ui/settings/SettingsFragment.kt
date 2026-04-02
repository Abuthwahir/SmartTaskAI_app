package com.smarttask.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.smarttask.databinding.FragmentSettingsBinding
import com.smarttask.utils.PreferencesManager
import com.smarttask.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()
        setupStats()
        setupApiKeyInput()
    }

    private fun setupSettings() {
        // Dark mode
        binding.switchDarkMode.isChecked = preferencesManager.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Notifications
        binding.switchNotifications.isChecked = preferencesManager.isNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationsEnabled(isChecked)
        }

        // AI Features
        binding.switchAiFeatures.isChecked = preferencesManager.isAiEnabled()
        binding.switchAiFeatures.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setAiEnabled(isChecked)
        }

        // AI Prioritize button
        binding.buttonAiPrioritize.setOnClickListener {
            viewModel.prioritizeTasksWithAI()
        }
    }

    private fun setupStats() {
        viewModel.loadStats()
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.textTotalTasks.text = stats.total.toString()
            binding.textCompletedTasks.text = stats.completed.toString()
            binding.textPendingTasks.text = stats.pending.toString()
            binding.textCompletionRate.text = "${stats.completionRate}%"
            binding.textHighPriority.text = stats.highPriority.toString()
            binding.progressCompletion.progress = stats.completionRate
        }
    }

    private fun setupApiKeyInput() {
        val savedKey = preferencesManager.getApiKey()
        if (savedKey.isNotBlank()) {
            binding.editTextApiKey.setText("•".repeat(minOf(savedKey.length, 20)))
        }

        binding.buttonSaveApiKey.setOnClickListener {
            val key = binding.editTextApiKey.text?.toString() ?: ""
            if (key.isNotBlank() && !key.contains("•")) {
                preferencesManager.setApiKey(key)
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root, "API key saved!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
