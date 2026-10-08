package com.ecoshare.app.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadImage(imageUri: Uri, folder: String = "resources"): String {
        val fileName = "${folder}/${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
