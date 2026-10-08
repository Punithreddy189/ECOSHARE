package com.ecoshare.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ItemEventBinding
import com.ecoshare.app.model.CommunityEvent

class EventAdapter(
    private val currentUserId: String,
    private val onRsvpClick: (CommunityEvent) -> Unit
) : ListAdapter<CommunityEvent, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CommunityEvent) {
            binding.tvEventTitle.text = event.title
            binding.tvEventTypeBadge.text = event.type
            binding.tvOrganizer.text = "by ${event.organizerName}"
            binding.tvEventDescription.text = event.description
            binding.tvEventDate.text = event.date
            binding.tvEventLocation.text = event.location

            val count = event.attendees.size
            binding.tvAttendeesCount.text = "$count ${if (count == 1) "Neighbor" else "Neighbors"} Attending"

            val isAttending = event.attendees.contains(currentUserId)
            if (isAttending) {
                binding.btnRsvp.text = "Attending ✓"
                binding.btnRsvp.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryLight))
                binding.btnRsvp.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryDark))
            } else {
                binding.btnRsvp.text = "RSVP"
                binding.btnRsvp.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
                binding.btnRsvp.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_white))
            }

            binding.btnRsvp.setOnClickListener {
                onRsvpClick(event)
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<CommunityEvent>() {
        override fun areItemsTheSame(oldItem: CommunityEvent, newItem: CommunityEvent): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: CommunityEvent, newItem: CommunityEvent): Boolean {
            return oldItem == newItem
        }
    }
}
