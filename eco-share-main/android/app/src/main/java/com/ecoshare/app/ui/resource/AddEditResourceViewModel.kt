package com.ecoshare.app.ui.resource

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.ResourceRepository
import com.ecoshare.app.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class AddEditUiState {
    object Idle : AddEditUiState()
    data class Loading(val message: String) : AddEditUiState()
    object Success : AddEditUiState()
    data class Error(val message: String) : AddEditUiState()
}

data class FormValidationResult(
    val titleError: String? = null,
    val listingTypeError: String? = null,
    val categoryError: String? = null,
    val quantityError: String? = null,
    val locationError: String? = null,
    val descriptionError: String? = null
) {
    val isValid: Boolean
        get() = titleError == null && listingTypeError == null && categoryError == null &&
                quantityError == null && locationError == null && descriptionError == null
}

class AddEditResourceViewModel(
    private val resourceRepo: ResourceRepository = ResourceRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditUiState>(AddEditUiState.Idle)
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun validateForm(
        title: String,
        listingType: String,
        category: String,
        quantity: String,
        location: String,
        description: String
    ): FormValidationResult {
        val titleErr = when {
            title.trim().isBlank() -> "Title is required"
            title.trim().length < 3 -> "Title must be at least 3 characters"
            else -> null
        }
        val typeErr = when {
            listingType.trim().isBlank() -> "Listing type is required"
            else -> null
        }
        val catErr = when {
            category.trim().isBlank() -> "Category is required"
            else -> null
        }
        val qtyErr = when {
            quantity.trim().isBlank() -> "Quantity is required"
            else -> null
        }
        val locErr = when {
            location.trim().isBlank() -> "Pick-up location is required"
            else -> null
        }
        val descErr = when {
            description.trim().isBlank() -> "Description is required"
            description.trim().length < 5 -> "Description must be at least 5 characters"
            else -> null
        }

        return FormValidationResult(
            titleError = titleErr,
            listingTypeError = typeErr,
            categoryError = catErr,
            quantityError = qtyErr,
            locationError = locErr,
            descriptionError = descErr
        )
    }

    fun saveResource(
        existingId: String?,
        title: String,
        listingType: String,
        category: String,
        quantity: String,
        location: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        localImageUri: Uri?,
        existingImageUrl: String
    ) {
        val validation = validateForm(
            title = title,
            listingType = listingType,
            category = category,
            quantity = quantity,
            location = location,
            description = description
        )

        if (!validation.isValid) {
            _uiState.value = AddEditUiState.Error("Please fix the highlighted fields.")
            return
        }

        val currentUser = authRepo.currentFirebaseUser
        val ownerId = currentUser?.uid ?: AuthRepository.currentMockUser.uid
        val ownerName = currentUser?.displayName ?: AuthRepository.currentMockUser.displayName

        viewModelScope.launch {
            try {
                _uiState.value = AddEditUiState.Loading("Saving resource...")

                val finalImageUrl = localImageUri?.toString() ?: existingImageUrl

                if (existingId.isNullOrBlank()) {
                    val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                    val newResource = Resource(
                        ownerId = ownerId,
                        ownerName = ownerName,
                        title = title.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        quantity = quantity.trim(),
                        imageUrl = finalImageUrl,
                        location = location.trim(),
                        latitude = latitude,
                        longitude = longitude,
                        status = "Available",
                        listingType = listingType.trim(),
                        price = if (listingType.contains("Rent", true)) "₹150 / day" else "Free",
                        ecoPoints = 40,
                        createdAt = now,
                        co2Offset = 3.0
                    )
                    resourceRepo.addResource(newResource)
                } else {
                    val updates = mutableMapOf<String, Any>(
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "category" to category.trim(),
                        "quantity" to quantity.trim(),
                        "listingType" to listingType.trim(),
                        "location" to location.trim(),
                        "imageUrl" to finalImageUrl
                    )
                    if (latitude != null) updates["latitude"] = latitude
                    if (longitude != null) updates["longitude"] = longitude
                    resourceRepo.updateResource(existingId, updates)
                }

                _uiState.value = AddEditUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddEditUiState.Error(e.localizedMessage ?: "Failed to save resource")
            }
        }
    }
}
