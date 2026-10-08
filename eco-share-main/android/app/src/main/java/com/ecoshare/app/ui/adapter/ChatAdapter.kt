package com.ecoshare.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ItemChatBinding
import com.ecoshare.app.model.Chat

class ChatAdapter(
    private val currentUserId: String,
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            val isLobby = chat.chatId == "general_lobby" || chat.isLobby

            if (isLobby) {
                binding.tvChatTitle.text = "Community Lobby"
                binding.tvChatAvatar.text = "🌐"
                binding.tvChatAvatar.setBackgroundResource(R.drawable.badge_shared)
                binding.tvChatAvatar.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
                binding.tvResourceContext.text = "Global Community Channel"
            } else {
                val otherId = chat.participants.find { it != currentUserId } ?: ""
                val partnerName = chat.participantNames[otherId] ?: "Resident"
                binding.tvChatTitle.text = partnerName
                binding.tvChatAvatar.text = partnerName.take(1).uppercase()
                binding.tvChatAvatar.setBackgroundResource(R.drawable.badge_available)
                binding.tvChatAvatar.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryDark))
                binding.tvResourceContext.text = "Re: ${chat.resourceTitle}"
            }

            binding.tvLastMessage.text = chat.lastMessage.ifBlank { "No messages yet" }
            binding.tvChatTime.text = formatTimestamp(chat.lastMessageAt)

            binding.root.setOnClickListener {
                onChatClick(chat)
            }
        }

        private fun formatTimestamp(isoString: String): String {
            if (isoString.isBlank()) return ""
            return try {
                if (isoString.length >= 16) {
                    isoString.substring(11, 16)
                } else isoString
            } catch (e: Exception) {
                ""
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
