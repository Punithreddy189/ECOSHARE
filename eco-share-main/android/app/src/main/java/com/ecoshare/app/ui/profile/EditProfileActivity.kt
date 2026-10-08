package com.ecoshare.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ecoshare.app.data.AuthRepository
import com.ecoshare.app.data.StorageRepository
import com.ecoshare.app.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val authRepository = AuthRepository()
    private val storageRepository = StorageRepository()

    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.tvAvatarFallback.visibility = View.GONE
            binding.ivProfileImage.visibility = View.VISIBLE
            binding.ivProfileImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = AuthRepository.currentMockUser
        val name = intent.getStringExtra("EXTRA_NAME") ?: currentUser.displayName
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: currentUser.email
        val location = intent.getStringExtra("EXTRA_LOCATION") ?: currentUser.location
        existingImageUrl = intent.getStringExtra("EXTRA_IMAGE_URL") ?: currentUser.profileImageUrl

        binding.etDisplayName.setText(name)
        binding.etEmail.setText(email)
        binding.etLocation.setText(location)

        if (!existingImageUrl.isNullOrBlank()) {
            binding.tvAvatarFallback.visibility = View.GONE
            binding.ivProfileImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(existingImageUrl)
                .circleCrop()
                .into(binding.ivProfileImage)
        } else {
            binding.tvAvatarFallback.visibility = View.VISIBLE
            binding.ivProfileImage.visibility = View.GONE
            binding.tvAvatarFallback.text = name.take(1).ifBlank { "P" }.uppercase()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val displayName = binding.etDisplayName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val location = binding.etLocation.text?.toString()?.trim() ?: ""

        if (displayName.isBlank()) {
            binding.tvErrorBanner.visibility = View.VISIBLE
            binding.tvErrorBanner.text = "Please enter your name"
            return
        }

        binding.tvErrorBanner.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val uid = authRepository.currentFirebaseUser?.uid ?: AuthRepository.currentMockUser.uid
                var finalImageUrl = existingImageUrl

                if (selectedImageUri != null) {
                    try {
                        finalImageUrl = storageRepository.uploadImage(selectedImageUri!!, folder = "avatars")
                    } catch (_: Exception) {
                        finalImageUrl = selectedImageUri.toString()
                    }
                }

                authRepository.updateProfile(
                    uid = uid,
                    displayName = displayName,
                    location = location,
                    profileImageUrl = finalImageUrl,
                    email = email
                )

                Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                binding.tvErrorBanner.visibility = View.VISIBLE
                binding.tvErrorBanner.text = e.message ?: "Failed to update profile."
            }
        }
    }
}
