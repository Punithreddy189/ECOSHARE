package com.ecoshare.app.data

import android.util.Log
import com.ecoshare.app.model.CommunityEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "EcoShare"

        // In-memory mock/fallback community events
        private val mockEvents = mutableListOf(
            CommunityEvent(
                eventId = "evt_1",
                title = "Neighborhood Tool Repair Cafe",
                type = "Workshop",
                date = "Saturday, Oct 14 • 10:00 AM",
                location = "Downtown Community Center (Room 102)",
                organizerName = "EcoShare Green Guild",
                organizerId = "org_1",
                description = "Bring broken electronics, bicycles, small appliances, or garments. Volunteer repair technicians will help diagnose and fix them for free to reduce landfill waste!",
                attendees = listOf("user_punit", "user_2", "user_3", "user_4", "user_5"),
                createdAt = "2026-10-05T09:00:00Z"
            ),
            CommunityEvent(
                eventId = "evt_2",
                title = "Urban Tree Planting & Compost Drive",
                type = "Clean-up Drive",
                date = "Sunday, Oct 22 • 09:00 AM",
                location = "Greenway Urban Park (East Gate)",
                organizerName = "Marcus Green",
                organizerId = "org_2",
                description = "Join our community initiative to plant 50 native shade trees along the pedestrian greenway and distribute mature compost to home gardeners.",
                attendees = listOf("user_punit", "user_6", "user_7"),
                createdAt = "2026-10-06T10:00:00Z"
            ),
            CommunityEvent(
                eventId = "evt_3",
                title = "Zero-Waste Cooking & Preserving Workshop",
                type = "Workshop",
                date = "Wednesday, Oct 25 • 06:30 PM",
                location = "EcoHub Community Kitchen",
                organizerName = "Sarah Chen",
                organizerId = "org_3",
                description = "Learn how to preserve seasonal fruit surpluses, make scrap vegetable broths, and eliminate kitchen waste with culinary chef Elena.",
                attendees = listOf("user_8", "user_9"),
                createdAt = "2026-10-07T11:00:00Z"
            ),
            CommunityEvent(
                eventId = "evt_4",
                title = "Autumn Backyard Seed & Cutting Swap",
                type = "Swap Meet",
                date = "Saturday, Nov 04 • 11:00 AM",
                location = "Botanical Garden Pavilion",
                organizerName = "David Miller",
                organizerId = "org_4",
                description = "Bring heirloom seeds, potted cuttings, and spare plant pots to exchange with fellow neighborhood green thumbs.",
                attendees = listOf("user_punit", "user_10", "user_11", "user_12"),
                createdAt = "2026-10-07T12:00:00Z"
            )
        )
    }

    fun getEventsFlow(): Flow<List<CommunityEvent>> = callbackFlow {
        Log.d(TAG, "EventRepository: getEventsFlow started")
        // Immediate mock emit for instant UI response
        trySend(mockEvents.toList())

        val query = firestore.collection("events")
            .orderBy("date", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "EventRepository: Firestore read fallback: ${error.message}")
                trySend(mockEvents.toList())
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CommunityEvent::class.java)?.apply { this.eventId = doc.id }
                }
                Log.d(TAG, "EventRepository: Firestore returned ${list.size} events")
                val combined = (list + mockEvents).distinctBy { it.eventId }
                trySend(combined)
            } else {
                trySend(mockEvents.toList())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addEvent(event: CommunityEvent): String {
        Log.d(TAG, "EventRepository: addEvent '${event.title}'")
        val generatedId = "evt_" + UUID.randomUUID().toString().take(8)
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        event.eventId = generatedId
        event.createdAt = now

        mockEvents.add(0, event)

        try {
            val docRef = firestore.collection("events").add(event).await()
            event.eventId = docRef.id
            docRef.update("eventId", docRef.id).await()
            return docRef.id
        } catch (e: Exception) {
            Log.w(TAG, "EventRepository: Firestore addEvent offline fallback: ${e.message}")
            return generatedId
        }
    }

    suspend fun toggleRsvp(eventId: String, userId: String, isAttending: Boolean) {
        Log.d(TAG, "EventRepository: toggleRsvp eventId=$eventId, userId=$userId, isAttending=$isAttending")
        val local = mockEvents.find { it.eventId == eventId }
        if (local != null) {
            val currentList = local.attendees.toMutableList()
            if (isAttending) {
                currentList.remove(userId)
            } else {
                if (!currentList.contains(userId)) currentList.add(userId)
            }
            local.attendees = currentList
        }

        try {
            val eventRef = firestore.collection("events").document(eventId)
            val update = if (isAttending) {
                FieldValue.arrayRemove(userId)
            } else {
                FieldValue.arrayUnion(userId)
            }
            eventRef.update("attendees", update).await()
        } catch (e: Exception) {
            Log.w(TAG, "EventRepository: toggleRsvp offline fallback: ${e.message}")
        }
    }
}
