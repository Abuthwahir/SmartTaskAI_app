package com.smarttask.ui.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smarttask.databinding.FragmentAssistantBinding
import com.smarttask.viewmodel.AssistantViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantFragment : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AssistantViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    private val quickActions = listOf(
        "Add Meeting at 10 AM tomorrow",
        "Schedule Workout for 6 PM today",
        "Remind me to drink water every 2 hours",
        "Set a study reminder for this weekend"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupQuickActions()
        setupInput()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@AssistantFragment.adapter
        }
    }

    private fun setupQuickActions() {
        quickActions.forEach { action ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = action
                isClickable = true
                setOnClickListener { viewModel.sendMessage(action) }
            }
            binding.chipGroupQuickActions.addView(chip)
        }
    }

    private fun setupInput() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text?.toString()?.trim() ?: return@setOnClickListener
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.editTextMessage.setText("")
            }
        }

        binding.buttonClearChat.setOnClickListener {
            viewModel.clearChat()
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            } else {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            }
        }

        viewModel.isTyping.observe(viewLifecycleOwner) { typing ->
            binding.typingIndicator.visibility = if (typing) View.VISIBLE else View.GONE
            binding.buttonSend.isEnabled = !typing
        }

        viewModel.createdTask.observe(viewLifecycleOwner) { task ->
            task ?: return@observe
            Snackbar.make(binding.root, "✅ Task created: ${task.title}", Snackbar.LENGTH_SHORT).show()
            viewModel.clearCreatedTask()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
