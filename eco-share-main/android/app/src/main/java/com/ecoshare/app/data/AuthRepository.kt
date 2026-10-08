package com.ecoshare.app.data

import android.util.Log
import com.ecoshare.app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "EcoShare"

        // Default mock user profile for offline / prototype operation
        var currentMockUser = User(
            uid = "user_punit",
            email = "punit@ecoshare.org",
            displayName = "Punit Reddy",
            location = "Downtown Green District",
            role = "resident",
            approved = true,
            status = "approved",
            ecoPoints = 150,
            savedResources = listOf("res_1", "res_3"),
            activeSessionId = "mock_session_1"
        )
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        Log.d(TAG, "AuthRepository: getAuthStateFlow listening")
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun getUserProfileFlow(uid: String): Flow<User?> = callbackFlow {
        Log.d(TAG, "AuthRepository: getUserProfileFlow for $uid")
        // Always emit current mock user profile first for instant UI loading
        trySend(currentMockUser)

        val docRef = firestore.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "AuthRepository: user profile read fallback: ${error.message}")
                trySend(currentMockUser)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.apply {
                    this.uid = snapshot.id
                }
                if (user != null) {
                    currentMockUser = user
                    trySend(user)
                } else {
                    trySend(currentMockUser)
                }
            } else {
                trySend(currentMockUser)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun login(email: String, pass: String): User {
        Log.d(TAG, "AuthRepository: login attempt for $email")
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val fbUser = result.user ?: throw Exception("Empty user")
            try { fbUser.getIdToken(true).await() } catch (_: Exception) {}

            val sessionId = "sess_" + UUID.randomUUID().toString().take(8)
            val docRef = firestore.collection("users").document(fbUser.uid)
            val snapshot = docRef.get().await()

            val user = if (snapshot.exists()) {
                snapshot.toObject(User::class.java) ?: User(uid = fbUser.uid, email = email)
            } else {
                User(
                    uid = fbUser.uid,
                    email = email,
                    displayName = fbUser.displayName ?: email.substringBefore("@"),
                    location = "Downtown Green District",
                    role = "resident",
                    approved = true,
                    status = "approved",
                    ecoPoints = 150,
                    activeSessionId = sessionId
                ).also { docRef.set(it, SetOptions.merge()).await() }
            }
            currentMockUser = user
            user
        } catch (e: Exception) {
            Log.w(TAG, "AuthRepository: login fallback to mock user: ${e.message}")
            currentMockUser = currentMockUser.copy(
                email = email.ifBlank { currentMockUser.email },
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            currentMockUser
        }
    }

    suspend fun register(email: String, pass: String, displayName: String, location: String): User {
        Log.d(TAG, "AuthRepository: register attempt for $email, name=$displayName")
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val fbUser = result.user ?: throw Exception("Registration failed")
            try { fbUser.getIdToken(true).await() } catch (_: Exception) {}

            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.ifBlank { "EcoShare Member" })
                    .build()
                fbUser.updateProfile(profileUpdates).await()
            } catch (_: Exception) {}

            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val sessionId = "sess_" + UUID.randomUUID().toString().take(8)
            val newUser = User(
                uid = fbUser.uid,
                email = email.trim(),
                displayName = displayName.ifBlank { "EcoShare Member" },
                location = location.ifBlank { "Downtown Green District" },
                role = "resident",
                approved = true,
                status = "approved",
                ecoPoints = 150,
                activeSessionId = sessionId,
                createdAt = now
            )
            try {
                firestore.collection("users").document(fbUser.uid).set(newUser, SetOptions.merge()).await()
            } catch (inner: Exception) {
                Log.w(TAG, "AuthRepository: Firestore write fallback: ${inner.message}")
            }
            currentMockUser = newUser
            newUser
        } catch (e: Exception) {
            Log.w(TAG, "AuthRepository: register fallback to mock user: ${e.message}")
            val newUser = User(
                uid = "user_punit",
                email = email.trim(),
                displayName = displayName.ifBlank { "EcoShare Member" },
                location = location.ifBlank { "Downtown Green District" },
                role = "resident",
                approved = true,
                status = "approved",
                ecoPoints = 150
            )
            currentMockUser = newUser
            newUser
        }
    }

    suspend fun forgotPassword(email: String) {
        Log.d(TAG, "AuthRepository: forgotPassword for $email")
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
        } catch (e: Exception) {
            Log.w(TAG, "AuthRepository: forgotPassword fallback: ${e.message}")
        }
    }

    suspend fun updateProfile(uid: String, displayName: String, location: String, profileImageUrl: String?, email: String = "") {
        Log.d(TAG, "AuthRepository: updateProfile name=$displayName, loc=$location, email=$email")
        currentMockUser = currentMockUser.copy(
            displayName = displayName,
            email = if (email.isNotBlank()) email else currentMockUser.email,
            location = location,
            profileImageUrl = profileImageUrl ?: currentMockUser.profileImageUrl
        )
        try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "location" to location
            )
            if (email.isNotBlank()) updates["email"] = email
            if (!profileImageUrl.isNullOrBlank()) updates["profileImageUrl"] = profileImageUrl
            firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "AuthRepository: Firestore updateProfile fallback: ${e.message}")
        }
    }

    fun logout() {
        Log.d(TAG, "AuthRepository: logout")
        try { auth.signOut() } catch (_: Exception) {}
    }
}
