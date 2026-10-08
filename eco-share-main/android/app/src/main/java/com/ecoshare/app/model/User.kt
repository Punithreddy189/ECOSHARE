package com.ecoshare.app.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("displayName") @set:PropertyName("displayName") var displayName: String = "",
    @get:PropertyName("location") @set:PropertyName("location") var location: String = "Downtown Green District",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "resident",
    @get:PropertyName("approved") @set:PropertyName("approved") var approved: Boolean = true,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "approved",
    @get:PropertyName("ecoPoints") @set:PropertyName("ecoPoints") var ecoPoints: Int = 150,
    @get:PropertyName("savedResources") @set:PropertyName("savedResources") var savedResources: List<String> = emptyList(),
    @get:PropertyName("profileImageUrl") @set:PropertyName("profileImageUrl") var profileImageUrl: String = "",
    @get:PropertyName("activeSessionId") @set:PropertyName("activeSessionId") var activeSessionId: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: String = ""
)
