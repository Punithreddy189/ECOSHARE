package com.ecoshare.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecoshare.app.databinding.ItemMessageIncomingBinding
import com.ecoshare.app.databinding.ItemMessageOutgoingBinding
import com.ecoshare.app.model.Message

class MessageAdapter(
    private val currentUserId: String,
    private val isLobby: Boolean = false
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_OUTGOING = 1
        private const val VIEW_TYPE_INCOMING = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) VIEW_TYPE_OUTGOING else VIEW_TYPE_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_OUTGOING) {
            val binding = ItemMessageOutgoingBinding.inflate(inflater, parent, false)
            OutgoingViewHolder(binding)
        } else {
            val binding = ItemMessageIncomingBinding.inflate(inflater, parent, false)
            IncomingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (holder is OutgoingViewHolder) {
            holder.bind(msg)
        } else if (holder is IncomingViewHolder) {
            holder.bind(msg, isLobby)
        }
    }

    class OutgoingViewHolder(private val binding: ItemMessageOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            binding.tvMessageContent.text = msg.content
            binding.tvMessageTime.text = formatTime(msg.createdAt)
        }
    }

    class IncomingViewHolder(private val binding: ItemMessageIncomingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message, showSender: Boolean) {
            binding.tvMessageContent.text = msg.content
            binding.tvMessageTime.text = formatTime(msg.createdAt)
            if (showSender) {
                binding.tvSenderName.visibility = View.VISIBLE
                binding.tvSenderName.text = msg.senderName
            } else {
                binding.tvSenderName.visibility = View.GONE
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

private fun formatTime(iso: String): String {
    if (iso.length >= 16) {
        return iso.substring(11, 16)
    }
    return ""
}
