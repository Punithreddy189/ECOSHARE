package com.ecoshare.app.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class CommunityEvent(
    @get:PropertyName("eventId") @set:PropertyName("eventId") var eventId: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "Swap Meet",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("location") @set:PropertyName("location") var location: String = "",
    @get:PropertyName("organizerName") @set:PropertyName("organizerName") var organizerName: String = "",
    @get:PropertyName("organizerId") @set:PropertyName("organizerId") var organizerId: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("attendees") @set:PropertyName("attendees") var attendees: List<String> = emptyList(),
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: String = ""
) : Serializable
