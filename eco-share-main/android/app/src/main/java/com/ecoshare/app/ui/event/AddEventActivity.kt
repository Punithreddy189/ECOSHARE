package com.ecoshare.app.ui.event

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.EventRepository
import com.ecoshare.app.databinding.ActivityAddEventBinding
import com.ecoshare.app.model.CommunityEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val eventRepo = EventRepository()
    private val authRepo = AuthRepository()

    private val eventTypes = listOf("Swap Meet", "Repair Cafe", "Recycling Drive", "Workshop")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, eventTypes)
        binding.spnEventType.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPublishEvent.setOnClickListener {
            val title = binding.etEventTitle.text.toString().trim()
            val type = binding.spnEventType.selectedItem?.toString() ?: "Swap Meet"
            val date = binding.etEventDate.text.toString().trim()
            val location = binding.etEventLocation.text.toString().trim()
            val desc = binding.etEventDescription.text.toString().trim()

            if (title.isBlank() || date.isBlank() || location.isBlank() || desc.isBlank()) {
                Toast.makeText(this, "Please fill in all event details.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = authRepo.currentFirebaseUser
            if (user == null) {
                Toast.makeText(this, "Please log in to host events.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnPublishEvent.isEnabled = false
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val event = CommunityEvent(
                title = title,
                type = type,
                date = date,
                location = location,
                organizerName = user.displayName ?: user.email ?: "Community Member",
                organizerId = user.uid,
                description = desc,
                attendees = listOf(user.uid),
                createdAt = now
            )

            lifecycleScope.launch {
                try {
                    eventRepo.addEvent(event)
                    Toast.makeText(this@AddEventActivity, "Event published to community!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    binding.btnPublishEvent.isEnabled = true
                    Toast.makeText(this@AddEventActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
