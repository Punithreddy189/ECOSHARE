package com.ecoshare.app.data

import android.util.Log
import com.ecoshare.app.R
import com.ecoshare.app.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ResourceRepository {

    companion object {
        private const val TAG = "EcoShare"

        // In-memory mock list seeded with realistic demo resources for offline prototype
        private val mockResources = mutableListOf(
            Resource(
                resourceId = "res_1",
                ownerId = "owner_sarah",
                ownerName = "Sarah Chen",
                title = "Bosch Cordless Drill & Bit Set",
                description = "18V Lithium-ion cordless drill with 32-piece drill and screwdriver bit set. Fully charged and ready for home DIY.",
                category = "Tools & Equipment",
                quantity = "1",
                imageUrl = "",
                location = "Downtown Green District (0.4 km)",
                latitude = 45.5152,
                longitude = -122.6784,
                status = "Available",
                listingType = "Borrow / Free",
                condition = "Like New",
                price = "Free",
                ecoPoints = 40,
                createdAt = "2026-10-08T10:00:00Z",
                co2Offset = 6.2,
                imageResId = R.drawable.photo_drill
            ),
            Resource(
                resourceId = "res_2",
                ownerId = "owner_marcus",
                ownerName = "Marcus Green",
                title = "Organic Meyer Lemons (5kg box)",
                description = "Freshly harvested organic lemons from our backyard urban orchard. Juicy, pesticide-free, great for cooking or juicing.",
                category = "Food & Produce",
                quantity = "5 kg",
                imageUrl = "",
                location = "North Suburbs (1.2 km)",
                latitude = 45.5230,
                longitude = -122.6820,
                status = "Available",
                listingType = "Borrow / Free",
                condition = "Fresh",
                price = "Free",
                ecoPoints = 25,
                createdAt = "2026-10-08T09:30:00Z",
                co2Offset = 3.5,
                imageResId = R.drawable.photo_lemons
            ),
            Resource(
                resourceId = "res_3",
                ownerId = "owner_elena",
                ownerName = "Elena Rostova",
                title = "Coleman 4-Person Camping Tent",
                description = "Waterproof dome tent with rainfly and ground stakes. Used twice, pristine condition. Great for weekend trail trips.",
                category = "Sports & Outdoor",
                quantity = "1",
                imageUrl = "",
                location = "Westside Greenway (2.0 km)",
                latitude = 45.5110,
                longitude = -122.6650,
                status = "Available",
                listingType = "Borrow / Free",
                condition = "Excellent",
                price = "Free",
                ecoPoints = 50,
                createdAt = "2026-10-08T08:15:00Z",
                co2Offset = 8.0,
                imageResId = R.drawable.photo_tent
            ),
            Resource(
                resourceId = "res_4",
                ownerId = "user_punit",
                ownerName = "Punit Reddy",
                title = "Trek FX 2 Disc City Bike",
                description = "Lightweight hybrid city bike, 18-speed Shimano gears, hydraulic disc brakes. Includes U-lock and front basket.",
                category = "Transport",
                quantity = "1",
                imageUrl = "",
                location = "Downtown Green District",
                latitude = 45.5170,
                longitude = -122.6750,
                status = "Shared",
                listingType = "Rent",
                condition = "Good",
                price = "₹150 / day",
                ecoPoints = 65,
                createdAt = "2026-10-07T14:00:00Z",
                co2Offset = 12.5,
                imageResId = R.drawable.photo_bicycle
            ),
            Resource(
                resourceId = "res_5",
                ownerId = "user_punit",
                ownerName = "Punit Reddy",
                title = "Clean Code & System Design Books",
                description = "Classic engineering books by Robert Martin and Alex Xu. Looking to exchange for Renewable Energy or Botany titles.",
                category = "Books & Study",
                quantity = "2 books",
                imageUrl = "",
                location = "Downtown Green District",
                latitude = 45.5160,
                longitude = -122.6790,
                status = "Available",
                listingType = "Exchange",
                condition = "Like New",
                price = "Exchange",
                ecoPoints = 30,
                createdAt = "2026-10-07T11:20:00Z",
                co2Offset = 4.0,
                imageResId = R.drawable.photo_books
            ),
            Resource(
                resourceId = "res_6",
                ownerId = "owner_david",
                ownerName = "David Miller",
                title = "Stihl Electric Cordless Lawn Mower",
                description = "Quiet, zero-emissions lawn mower with 2 rechargeable batteries. Perfect for residential lawns without gas fumes.",
                category = "Garden & Outdoor",
                quantity = "1",
                imageUrl = "",
                location = "Oakwood Hills (3.5 km)",
                latitude = 45.5080,
                longitude = -122.6900,
                status = "Available",
                listingType = "Rent",
                condition = "Good",
                price = "₹200 / day",
                ecoPoints = 45,
                createdAt = "2026-10-06T16:45:00Z",
                co2Offset = 9.8,
                imageResId = R.drawable.photo_lawn_mower
            )
        )

        // Reactive StateFlow broadcasting real-time updates across the app
        private val _resourcesFlow = MutableStateFlow<List<Resource>>(mockResources.toList())
    }

    /**
     * Flow of all resources, shared synchronously across Home, Browse, and Profile.
     */
    fun getResourcesFlow(): Flow<List<Resource>> {
        return _resourcesFlow.asStateFlow()
    }

    fun getResourceById(id: String): Resource? {
        Log.d(TAG, "ResourceRepository: getResourceById $id")
        return synchronized(mockResources) {
            mockResources.find { it.resourceId == id }?.copy()
        }
    }

    fun addResource(resource: Resource): String {
        Log.d(TAG, "ResourceRepository: addResource '${resource.title}'")
        val generatedId = if (resource.resourceId.isNotBlank()) resource.resourceId else "res_" + UUID.randomUUID().toString().take(8)
        if (resource.createdAt.isBlank()) {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            resource.createdAt = now
        }
        resource.resourceId = generatedId

        synchronized(mockResources) {
            mockResources.add(0, resource)
            _resourcesFlow.value = mockResources.toList()
        }
        Log.d(TAG, "ResourceRepository: Resource added successfully, total count = ${mockResources.size}")
        return generatedId
    }

    fun updateResource(resourceId: String, updates: Map<String, Any>) {
        Log.d(TAG, "ResourceRepository: updateResource $resourceId")
        synchronized(mockResources) {
            val local = mockResources.find { it.resourceId == resourceId }
            if (local != null) {
                updates["title"]?.let { local.title = it as String }
                updates["description"]?.let { local.description = it as String }
                updates["category"]?.let { local.category = it as String }
                updates["quantity"]?.let { local.quantity = it as String }
                updates["listingType"]?.let { local.listingType = it as String }
                updates["location"]?.let { local.location = it as String }
                updates["imageUrl"]?.let { local.imageUrl = it as String }
                updates["status"]?.let { local.status = it as String }
                if (updates.containsKey("latitude")) local.latitude = updates["latitude"] as? Double
                if (updates.containsKey("longitude")) local.longitude = updates["longitude"] as? Double
            }
            _resourcesFlow.value = mockResources.toList()
        }
    }

    fun deleteResource(resourceId: String) {
        Log.d(TAG, "ResourceRepository: deleteResource $resourceId")
        synchronized(mockResources) {
            mockResources.removeAll { it.resourceId == resourceId }
            _resourcesFlow.value = mockResources.toList()
        }
    }

    fun toggleSaveResource(userId: String, resourceId: String, isCurrentlySaved: Boolean) {
        Log.d(TAG, "ResourceRepository: toggleSaveResource $resourceId, currentlySaved=$isCurrentlySaved")
        // Offline: state handled in AuthRepository / MainViewModel
    }
}
