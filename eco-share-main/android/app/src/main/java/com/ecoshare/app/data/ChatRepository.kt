package com.ecoshare.app.data

import android.util.Log
import com.ecoshare.app.model.Chat
import com.ecoshare.app.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "EcoShare"

        // In-memory mock conversations
        private val mockChats = mutableListOf(
            Chat(
                chatId = "general_lobby",
                resourceId = "general",
                resourceTitle = "Community Lobby",
                lastMessage = "Welcome everyone to our local eco-sharing network!",
                lastMessageAt = "Just now",
                lastMessageSenderId = "system",
                lastMessageSenderName = "EcoShare",
                isLobby = true
            ),
            Chat(
                chatId = "chat_1",
                participants = listOf("user_punit", "owner_sarah"),
                participantNames = mapOf("user_punit" to "Punit Reddy", "owner_sarah" to "Sarah Chen"),
                resourceId = "res_1",
                resourceTitle = "Bosch Cordless Drill & Bit Set",
                lastMessage = "Sure! You can pick up the drill kit today around 5 PM.",
                lastMessageAt = "10:45 AM",
                lastMessageSenderId = "owner_sarah",
                lastMessageSenderName = "Sarah Chen",
                isLobby = false
            ),
            Chat(
                chatId = "chat_2",
                participants = listOf("user_punit", "owner_david"),
                participantNames = mapOf("user_punit" to "Punit Reddy", "owner_david" to "David Miller"),
                resourceId = "res_6",
                resourceTitle = "Stihl Electric Cordless Lawn Mower",
                lastMessage = "Is the second battery pack fully charged?",
                lastMessageAt = "Yesterday",
                lastMessageSenderId = "user_punit",
                lastMessageSenderName = "Punit Reddy",
                isLobby = false
            )
        )

        // In-memory messages per chatId
        private val mockMessages = mutableMapOf<String, MutableList<Message>>(
            "general_lobby" to mutableListOf(
                Message(
                    messageId = "m_l1",
                    senderId = "system",
                    senderName = "EcoShare Team",
                    content = "Welcome to the Neighborhood EcoShare Lobby! Share requests, ask questions, and connect with neighbors.",
                    createdAt = "Yesterday"
                ),
                Message(
                    messageId = "m_l2",
                    senderId = "owner_marcus",
                    senderName = "Marcus Green",
                    content = "Hey everyone! We have spare Meyer lemons if anyone wants fresh citrus.",
                    createdAt = "09:15 AM"
                ),
                Message(
                    messageId = "m_l3",
                    senderId = "user_punit",
                    senderName = "Punit Reddy",
                    content = "That sounds wonderful, Marcus! I can bring some garden cuttings to swap.",
                    createdAt = "09:30 AM"
                )
            ),
            "chat_1" to mutableListOf(
                Message(
                    messageId = "m_1",
                    senderId = "user_punit",
                    senderName = "Punit Reddy",
                    content = "Hi Sarah! Is your Bosch cordless drill available to borrow this evening?",
                    createdAt = "10:30 AM"
                ),
                Message(
                    messageId = "m_2",
                    senderId = "owner_sarah",
                    senderName = "Sarah Chen",
                    content = "Hello Punit! Yes, it is fully charged with the bit set ready in the case.",
                    createdAt = "10:35 AM"
                ),
                Message(
                    messageId = "m_3",
                    senderId = "owner_sarah",
                    senderName = "Sarah Chen",
                    content = "Sure! You can pick up the drill kit today around 5 PM.",
                    createdAt = "10:45 AM"
                )
            ),
            "chat_2" to mutableListOf(
                Message(
                    messageId = "m_21",
                    senderId = "user_punit",
                    senderName = "Punit Reddy",
                    content = "Hi David, I'm interested in renting the lawn mower for tomorrow morning.",
                    createdAt = "Yesterday"
                ),
                Message(
                    messageId = "m_22",
                    senderId = "user_punit",
                    senderName = "Punit Reddy",
                    content = "Is the second battery pack fully charged?",
                    createdAt = "Yesterday"
                )
            )
        )
    }

    fun getUserChatsFlow(userId: String): Flow<List<Chat>> = callbackFlow {
        Log.d(TAG, "ChatRepository: getUserChatsFlow for $userId")
        // Immediate mock emit so Messages tab is never blank
        trySend(mockChats.toList())

        val lobbyDoc = firestore.collection("chats").document("general_lobby")
        val chatsQuery = firestore.collection("chats")
            .whereArrayContains("participants", userId)

        var remoteChats = listOf<Chat>()

        val chatsListener = chatsQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "ChatRepository: Firestore chats read fallback: ${error.message}")
                trySend(mockChats.toList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                remoteChats = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.apply { this.chatId = doc.id }
                }
                val combined = (mockChats + remoteChats).distinctBy { it.chatId }
                trySend(combined)
            } else {
                trySend(mockChats.toList())
            }
        }

        awaitClose { chatsListener.remove() }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "ChatRepository: getMessagesFlow for $chatId")
        val localList = mockMessages.getOrPut(chatId) { mutableListOf() }
        trySend(localList.toList())

        val messagesQuery = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listener = messagesQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "ChatRepository: messages read fallback for $chatId: ${error.message}")
                trySend(localList.toList())
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.apply { this.messageId = doc.id }
                }
                val combined = (localList + list).distinctBy { it.messageId }
                trySend(combined)
            } else {
                trySend(localList.toList())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, senderId: String, senderName: String, content: String) {
        Log.d(TAG, "ChatRepository: sendMessage in $chatId from $senderName: '$content'")
        val now = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val message = Message(
            messageId = "m_" + UUID.randomUUID().toString().take(8),
            senderId = senderId,
            senderName = senderName,
            content = content,
            createdAt = now
        )

        // Store locally immediately for responsive messaging UI
        val msgList = mockMessages.getOrPut(chatId) { mutableListOf() }
        msgList.add(message)

        val chat = mockChats.find { it.chatId == chatId }
        if (chat != null) {
            chat.lastMessage = content
            chat.lastMessageAt = now
            chat.lastMessageSenderId = senderId
            chat.lastMessageSenderName = senderName
        }

        // Try syncing to Firestore
        try {
            val msgRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message).await()
            msgRef.update("messageId", msgRef.id).await()

            val chatUpdates = mapOf(
                "lastMessage" to content,
                "lastMessageAt" to now,
                "lastMessageSenderId" to senderId,
                "lastMessageSenderName" to senderName
            )
            firestore.collection("chats").document(chatId).set(chatUpdates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "ChatRepository: Firestore sendMessage offline fallback: ${e.message}")
        }
    }

    suspend fun getOrCreateChat(
        otherUserId: String,
        resourceId: String,
        resourceTitle: String,
        otherUserName: String,
        currentUserId: String,
        currentUserName: String
    ): Chat {
        Log.d(TAG, "ChatRepository: getOrCreateChat for resource '$resourceTitle'")
        val existing = mockChats.find {
            it.resourceId == resourceId && it.participants.contains(otherUserId) && it.participants.contains(currentUserId)
        }
        if (existing != null) return existing

        val now = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val newChat = Chat(
            chatId = "chat_" + UUID.randomUUID().toString().take(8),
            participants = listOf(currentUserId, otherUserId),
            participantNames = mapOf(
                currentUserId to currentUserName,
                otherUserId to otherUserName
            ),
            resourceId = resourceId,
            resourceTitle = resourceTitle,
            lastMessage = "Conversation started",
            lastMessageAt = now,
            lastMessageSenderId = currentUserId,
            lastMessageSenderName = currentUserName,
            isLobby = false
        )

        mockChats.add(1, newChat)
        return newChat
    }
}
