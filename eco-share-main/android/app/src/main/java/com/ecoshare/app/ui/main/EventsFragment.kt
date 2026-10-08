package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.databinding.FragmentEventsBinding
import com.ecoshare.app.ui.adapter.EventAdapter
import com.ecoshare.app.ui.event.AddEventActivity

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("EcoShare", "EventsFragment loaded")
        val currentUid = viewModel.currentUser.value?.uid ?: ""

        eventAdapter = EventAdapter(currentUid) { event ->
            viewModel.toggleEventRsvp(event.eventId)
        }
        binding.rvEvents.adapter = eventAdapter

        binding.btnHostEvent.setOnClickListener {
            startActivity(Intent(requireContext(), AddEventActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.events.collect { list ->
                eventAdapter.submitList(list)
                binding.layoutEmptyEvents.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
