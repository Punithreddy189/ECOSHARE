package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.databinding.FragmentMessagesBinding
import com.ecoshare.app.model.Chat
import com.ecoshare.app.ui.adapter.ChatAdapter
import com.ecoshare.app.ui.chat.ChatActivity

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("EcoShare", "MessagesFragment loaded")
        val currentUid = viewModel.currentUser.value?.uid ?: ""

        // Community Lobby click
        binding.cardCommunityLobby.setOnClickListener {
            val lobbyChat = Chat(
                chatId = "general_lobby",
                resourceTitle = "Community Lobby",
                isLobby = true
            )
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("EXTRA_CHAT", lobbyChat)
            }
            startActivity(intent)
        }

        chatAdapter = ChatAdapter(currentUid) { chat ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("EXTRA_CHAT", chat)
            }
            startActivity(intent)
        }
        binding.rvChats.adapter = chatAdapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.chats.collect { list ->
                // Filter out general_lobby from private conversations list since it has its dedicated card
                val privateChats = list.filter { it.chatId != "general_lobby" }
                chatAdapter.submitList(privateChats)
                binding.layoutEmptyChats.visibility = if (privateChats.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
