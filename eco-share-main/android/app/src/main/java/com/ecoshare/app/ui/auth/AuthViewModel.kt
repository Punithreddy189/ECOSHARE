package com.ecoshare.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class PasswordResetSent(val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter email and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = authRepo.login(email, pass)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Login failed. Check your credentials.")
            }
        }
    }

    fun register(email: String, pass: String, name: String, location: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = authRepo.register(email, pass, name, location)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed.")
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepo.forgotPassword(email)
                _uiState.value = AuthUiState.PasswordResetSent(email)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Could not send reset email.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
