package com.ecoshare.app.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Message(
    @get:PropertyName("messageId") @set:PropertyName("messageId") var messageId: String = "",
    @get:PropertyName("senderId") @set:PropertyName("senderId") var senderId: String = "",
    @get:PropertyName("senderName") @set:PropertyName("senderName") var senderName: String = "",
    @get:PropertyName("content") @set:PropertyName("content") var content: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: String = ""
) : Serializable
