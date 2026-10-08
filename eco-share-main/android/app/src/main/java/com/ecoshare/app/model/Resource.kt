package com.ecoshare.app.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Resource(
    @get:PropertyName("resourceId") @set:PropertyName("resourceId") var resourceId: String = "",
    @get:PropertyName("ownerId") @set:PropertyName("ownerId") var ownerId: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "Tools & Equipment",
    @get:PropertyName("quantity") @set:PropertyName("quantity") var quantity: String = "1",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String = "",
    @get:PropertyName("location") @set:PropertyName("location") var location: String = "",
    @get:PropertyName("latitude") @set:PropertyName("latitude") var latitude: Double? = null,
    @get:PropertyName("longitude") @set:PropertyName("longitude") var longitude: Double? = null,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "Available",
    @get:PropertyName("listingType") @set:PropertyName("listingType") var listingType: String = "Borrow / Free",
    @get:PropertyName("condition") @set:PropertyName("condition") var condition: String = "Good",
    @get:PropertyName("price") @set:PropertyName("price") var price: String = "Free",
    @get:PropertyName("ecoPoints") @set:PropertyName("ecoPoints") var ecoPoints: Int = 35,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: String = "",
    @get:PropertyName("co2Offset") @set:PropertyName("co2Offset") var co2Offset: Double = 2.5,
    @get:PropertyName("imageResId") @set:PropertyName("imageResId") var imageResId: Int = 0
) : Serializable
