package com.ecoshare.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.ChatRepository
import com.ecoshare.app.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepo: ChatRepository = ChatRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    val currentUserId: String
        get() = authRepo.currentFirebaseUser?.uid ?: AuthRepository.currentMockUser.uid

    val currentUserName: String
        get() = authRepo.currentFirebaseUser?.displayName ?: AuthRepository.currentMockUser.displayName

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepo.getMessagesFlow(chatId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        if (content.isBlank() || currentUserId.isBlank()) return
        viewModelScope.launch {
            chatRepo.sendMessage(chatId, currentUserId, currentUserName, content.trim())
        }
    }
}
