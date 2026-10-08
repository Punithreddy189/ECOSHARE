package com.ecoshare.app.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Chat(
    @get:PropertyName("chatId") @set:PropertyName("chatId") var chatId: String = "",
    @get:PropertyName("participants") @set:PropertyName("participants") var participants: List<String> = emptyList(),
    @get:PropertyName("participantNames") @set:PropertyName("participantNames") var participantNames: Map<String, String> = emptyMap(),
    @get:PropertyName("resourceId") @set:PropertyName("resourceId") var resourceId: String = "",
    @get:PropertyName("resourceTitle") @set:PropertyName("resourceTitle") var resourceTitle: String = "",
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage") var lastMessage: String = "",
    @get:PropertyName("lastMessageAt") @set:PropertyName("lastMessageAt") var lastMessageAt: String = "",
    @get:PropertyName("lastMessageSenderId") @set:PropertyName("lastMessageSenderId") var lastMessageSenderId: String = "",
    @get:PropertyName("lastMessageSenderName") @set:PropertyName("lastMessageSenderName") var lastMessageSenderName: String = "",
    @get:PropertyName("isLobby") @set:PropertyName("isLobby") var isLobby: Boolean = false
) : Serializable
