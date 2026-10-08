package com.ecoshare.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.ChatRepository
import com.ecoshare.app.data.EventRepository
import com.ecoshare.app.data.ResourceRepository
import com.ecoshare.app.model.Chat
import com.ecoshare.app.model.CommunityEvent
import com.ecoshare.app.model.Resource
import com.ecoshare.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ResourceTab {
    RECENT, NEARBY, SAVED, MY_SHARES
}

class MainViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val resourceRepo: ResourceRepository = ResourceRepository(),
    private val chatRepo: ChatRepository = ChatRepository(),
    private val eventRepo: EventRepository = EventRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allResources = MutableStateFlow<List<Resource>>(emptyList())
    val allResources: StateFlow<List<Resource>> = _allResources.asStateFlow()

    val selectedCategory = MutableStateFlow("All")
    val selectedTab = MutableStateFlow(ResourceTab.RECENT)
    val searchQuery = MutableStateFlow("")
    val selectedStatus = MutableStateFlow("All")
    val selectedListingType = MutableStateFlow("All Types")

    var userLat: Double? = null
    var userLng: Double? = null

    val communityImpact: StateFlow<Triple<Int, Double, Int>> = _allResources.combine(_currentUser) { list, _ ->
        val itemsShared = list.count { it.status.equals("Shared", true) || it.status.equals("Completed", true) }.coerceAtLeast(list.size)
        val co2 = list.sumOf { it.co2Offset }
        val co2Offset = if (co2 > 0) co2 else (itemsShared * 2.5)
        val distinctNeighbors = list.map { it.ownerId }.filter { it.isNotBlank() }.distinct().size.coerceAtLeast(1)
        Triple(itemsShared, co2Offset, distinctNeighbors)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(0, 0.0, 0))

    val filteredResources: StateFlow<List<Resource>> = combine(
        _allResources,
        _currentUser,
        selectedCategory,
        selectedTab,
        searchQuery,
        selectedStatus,
        selectedListingType
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val resources = args[0] as List<Resource>
        val user = args[1] as? User
        val category = args[2] as String
        val tab = args[3] as ResourceTab
        val query = args[4] as String
        val status = args[5] as String
        val listingType = args[6] as String

        var list = resources

        // 1. Category
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 2. Status
        if (status != "All") {
            list = list.filter { it.status.equals(status, ignoreCase = true) }
        }

        // 3. Listing Type (for Browse tab)
        if (listingType != "All Types") {
            val key = listingType.substringBefore(" /").trim()
            list = list.filter { it.listingType.contains(key, ignoreCase = true) }
        }

        // 4. Tab filter
        list = when (tab) {
            ResourceTab.RECENT -> list
            ResourceTab.SAVED -> {
                val saved = user?.savedResources ?: emptyList()
                list.filter { saved.contains(it.resourceId) }
            }
            ResourceTab.MY_SHARES -> {
                val uid = user?.uid ?: ""
                list.filter { it.ownerId == uid }
            }
            ResourceTab.NEARBY -> {
                val uLat = userLat ?: 45.5152
                val uLng = userLng ?: -122.6784
                list.sortedBy { res ->
                    if (res.latitude != null && res.longitude != null) {
                        calculateDistance(uLat, uLng, res.latitude!!, res.longitude!!)
                    } else Double.MAX_VALUE
                }
            }
        }

        // 5. Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) || it.description.lowercase().contains(q) || it.location.lowercase().contains(q) || it.category.lowercase().contains(q)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _events = MutableStateFlow<List<CommunityEvent>>(emptyList())
    val events: StateFlow<List<CommunityEvent>> = _events.asStateFlow()

    private val activeDataJobs = mutableListOf<kotlinx.coroutines.Job>()

    init {
        loadData()
    }

    private fun loadData() {
        android.util.Log.d("EcoShare", "MainViewModel: initializing prototype and repository streams")
        _currentUser.value = AuthRepository.currentMockUser

        viewModelScope.launch {
            authRepo.getUserProfileFlow("user_punit").collect { user ->
                _currentUser.value = user ?: AuthRepository.currentMockUser
            }
        }
        viewModelScope.launch {
            resourceRepo.getResourcesFlow().collect { list ->
                android.util.Log.d("EcoShare", "MainViewModel: loaded ${list.size} resources")
                _allResources.value = list
            }
        }
        viewModelScope.launch {
            eventRepo.getEventsFlow().collect { list ->
                android.util.Log.d("EcoShare", "MainViewModel: loaded ${list.size} events")
                _events.value = list
            }
        }
        viewModelScope.launch {
            chatRepo.getUserChatsFlow("user_punit").collect { list ->
                android.util.Log.d("EcoShare", "MainViewModel: loaded ${list.size} chats")
                _chats.value = list
            }
        }
    }

    fun refreshUserProfile() {
        _currentUser.value = AuthRepository.currentMockUser
    }

    fun toggleBookmark(resourceId: String) {
        val user = _currentUser.value ?: AuthRepository.currentMockUser
        val isSaved = user.savedResources.contains(resourceId)
        val updated = if (isSaved) {
            user.savedResources.filter { it != resourceId }
        } else {
            user.savedResources + resourceId
        }
        val newUser = user.copy(savedResources = updated)
        _currentUser.value = newUser
        AuthRepository.currentMockUser = newUser
        viewModelScope.launch {
            resourceRepo.toggleSaveResource(user.uid, resourceId, isSaved)
        }
    }

    fun toggleEventRsvp(eventId: String) {
        val user = _currentUser.value ?: AuthRepository.currentMockUser
        val event = events.value.find { it.eventId == eventId } ?: return
        val isAttending = event.attendees.contains(user.uid)
        viewModelScope.launch {
            eventRepo.toggleRsvp(eventId, user.uid, isAttending)
        }
    }

    fun logout() {
        authRepo.logout()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
