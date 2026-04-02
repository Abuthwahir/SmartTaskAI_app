package com.smarttask.ui.assistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smarttask.database.entities.ChatMessageEntity
import com.smarttask.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatAdapter — renders user and AI assistant messages in the chat RecyclerView.
 *
 * Uses unique view IDs for each message type (textMessageUser / textMessageAi etc.)
 * to avoid the ViewBinding ambiguity that occurs when duplicate IDs exist in the same layout.
 */
class ChatAdapter : ListAdapter<ChatMessageEntity, ChatAdapter.MessageViewHolder>(DiffCallback) {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<ChatMessageEntity>() {
            override fun areItemsTheSame(old: ChatMessageEntity, new: ChatMessageEntity) =
                old.id == new.id
            override fun areContentsTheSame(old: ChatMessageEntity, new: ChatMessageEntity) =
                old == new
        }
    }

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessageEntity) {
            val isUser = message.role == "user"
            val timestamp = formatTime(message.timestamp)

            if (isUser) {
                // Show user bubble, hide assistant bubble
                binding.layoutUser.visibility = View.VISIBLE
                binding.layoutAssistant.visibility = View.GONE

                binding.textMessageUser.text = message.content
                binding.textTimestampUser.text = timestamp

            } else {
                // Show assistant bubble, hide user bubble
                binding.layoutUser.visibility = View.GONE
                binding.layoutAssistant.visibility = View.VISIBLE

                binding.textMessageAi.text = message.content
                binding.textTimestampAi.text = timestamp

                // Show "Task created" badge only when a task was auto-created from this message
                binding.badgeTaskCreated.visibility =
                    if (message.taskId != null) View.VISIBLE else View.GONE
            }
        }

        private fun formatTime(timestamp: Long): String =
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
