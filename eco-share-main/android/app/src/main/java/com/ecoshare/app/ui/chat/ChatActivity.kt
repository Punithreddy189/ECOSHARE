package com.ecoshare.app.ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.databinding.ActivityChatBinding
import com.ecoshare.app.model.Chat
import com.ecoshare.app.ui.adapter.MessageAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private var currentChat: Chat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentChat = intent.getSerializableExtra("EXTRA_CHAT") as? Chat
        if (currentChat == null) {
            finish()
            return
        }

        setupHeader()
        setupRecyclerView()
        setupSendListener()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener { finish() }

        val chat = currentChat!!
        val isLobby = chat.chatId == "general_lobby" || chat.isLobby

        if (isLobby) {
            binding.tvChatPartnerName.text = "Community Lobby"
            binding.tvChatContext.text = "Global Community Channel"
        } else {
            val otherId = chat.participants.find { it != viewModel.currentUserId } ?: ""
            val partnerName = chat.participantNames[otherId] ?: "Resident"
            binding.tvChatPartnerName.text = partnerName
            binding.tvChatContext.text = "Re: ${chat.resourceTitle}"
        }
    }

    private fun setupRecyclerView() {
        val chat = currentChat!!
        val isLobby = chat.chatId == "general_lobby" || chat.isLobby
        val adapter = MessageAdapter(viewModel.currentUserId, isLobby)
        binding.rvMessages.adapter = adapter

        viewModel.loadMessages(chat.chatId)

        lifecycleScope.launchWhenStarted {
            viewModel.messages.collect { messages ->
                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    private fun setupSendListener() {
        binding.btnSendMessage.setOnClickListener {
            val content = binding.etMessageInput.text.toString().trim()
            if (content.isNotBlank()) {
                viewModel.sendMessage(currentChat!!.chatId, content)
                binding.etMessageInput.setText("")
            }
        }
    }
}
