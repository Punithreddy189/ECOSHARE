package com.ecoshare.app.ui.resource

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.ChatRepository
import com.ecoshare.app.data.ResourceRepository
import com.ecoshare.app.model.Chat
import com.ecoshare.app.model.Resource
import com.ecoshare.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Idle : DetailUiState()
    object Loading : DetailUiState()
    object Deleted : DetailUiState()
    data class ChatStarted(val chat: Chat) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class ResourceDetailViewModel(
    private val resourceRepo: ResourceRepository = ResourceRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val chatRepo: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _resource = MutableStateFlow<Resource?>(null)
    val resource: StateFlow<Resource?> = _resource.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        val fbUser = authRepo.currentFirebaseUser
        if (fbUser != null) {
            viewModelScope.launch {
                authRepo.getUserProfileFlow(fbUser.uid).collect {
                    _currentUser.value = it
                }
            }
        }
    }

    fun setResource(res: Resource) {
        _resource.value = res
    }

    fun loadResource(id: String) {
        viewModelScope.launch {
            val res = resourceRepo.getResourceById(id)
            if (res != null) {
                _resource.value = res
            }
        }
    }

    fun updateStatus(newStatus: String) {
        val res = _resource.value ?: return
        viewModelScope.launch {
            try {
                resourceRepo.updateResource(res.resourceId, mapOf("status" to newStatus))
                _resource.value = res.copy(status = newStatus)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Failed to update status")
            }
        }
    }

    fun requestResource() {
        updateStatus("Pending")
    }

    fun deleteResource() {
        val res = _resource.value ?: return
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                resourceRepo.deleteResource(res.resourceId)
                _uiState.value = DetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Failed to delete item")
            }
        }
    }

    fun toggleBookmark() {
        val user = _currentUser.value ?: return
        val res = _resource.value ?: return
        val isSaved = user.savedResources.contains(res.resourceId)
        viewModelScope.launch {
            resourceRepo.toggleSaveResource(user.uid, res.resourceId, isSaved)
        }
    }

    fun startChat() {
        val user = _currentUser.value ?: return
        val res = _resource.value ?: return
        if (user.uid == res.ownerId) {
            _uiState.value = DetailUiState.Error("You cannot message yourself.")
            return
        }
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val chat = chatRepo.getOrCreateChat(
                    otherUserId = res.ownerId,
                    resourceId = res.resourceId,
                    resourceTitle = res.title,
                    otherUserName = res.ownerName,
                    currentUserId = user.uid,
                    currentUserName = user.displayName
                )
                _uiState.value = DetailUiState.ChatStarted(chat)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Failed to open chat")
            }
        }
    }
}
