package com.ecoshare.app.ui.resource

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ActivityAddEditResourceBinding
import com.ecoshare.app.model.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class AddEditResourceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditResourceBinding
    private val viewModel: AddEditResourceViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String = ""
    private var existingResourceId: String? = null

    private var detectedLat: Double? = null
    private var detectedLng: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCancellationTokenSource: CancellationTokenSource? = null

    private val categories = listOf(
        "Tools & Equipment", "Food & Produce", "Books", "Electronics",
        "Clothes", "Gardening", "Sports & Outdoor", "Household Items", "Other"
    )

    private val listingTypes = listOf("Borrow / Free", "Rent", "Sell", "Exchange")

    // Image Pickers
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val localUri = persistImageToInternalStorage(uri)
            selectedImageUri = localUri
            showImagePreview(localUri)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val localUri = persistBitmapToInternalStorage(bitmap)
            selectedImageUri = localUri
            showImagePreview(localUri)
        }
    }

    // Location Permission Launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            fetchGpsLocation()
        } else {
            val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)

            if (permanentlyDenied) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("Location access has been disabled. You can enable it in App Settings or type your pickup location manually.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Manual Entry", null)
                    .show()
            } else {
                Toast.makeText(this, "Location permission is required to use GPS. Please enter it manually.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupDropdowns()
        setupTextWatchers()
        checkExistingResource()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actCategory.setAdapter(catAdapter)
        binding.actCategory.setText(categories[0], false)

        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listingTypes)
        binding.actListingType.setAdapter(typeAdapter)
        binding.actListingType.setText(listingTypes[0], false)
    }

    private fun setupTextWatchers() {
        binding.etTitle.doAfterTextChanged { binding.tilTitle.error = null }
        binding.actListingType.doAfterTextChanged { binding.tilListingType.error = null }
        binding.actCategory.doAfterTextChanged { binding.tilCategory.error = null }
        binding.etQuantity.doAfterTextChanged { binding.tilQuantity.error = null }
        binding.etLocation.doAfterTextChanged { binding.tilLocation.error = null }
        binding.etDescription.doAfterTextChanged { binding.tilDescription.error = null }
    }

    private fun checkExistingResource() {
        val resource = intent.getSerializableExtra("EXTRA_RESOURCE") as? Resource
        if (resource != null) {
            existingResourceId = resource.resourceId
            existingImageUrl = resource.imageUrl
            detectedLat = resource.latitude
            detectedLng = resource.longitude

            binding.tvHeaderTitle.text = "Edit Resource"
            binding.etTitle.setText(resource.title)
            binding.etQuantity.setText(resource.quantity)
            binding.etLocation.setText(resource.location)
            binding.etDescription.setText(resource.description)

            val cat = if (categories.contains(resource.category)) resource.category else categories[0]
            binding.actCategory.setText(cat, false)

            val type = if (listingTypes.contains(resource.listingType)) resource.listingType else listingTypes[0]
            binding.actListingType.setText(type, false)

            if (resource.imageUrl.isNotBlank()) {
                Glide.with(this).load(resource.imageUrl).into(binding.ivPreview)
                binding.ivPreview.visibility = View.VISIBLE
                binding.layoutImagePicker.visibility = View.GONE
                binding.btnRemoveImage.visibility = View.VISIBLE
            } else if (resource.imageResId != 0) {
                binding.ivPreview.setImageResource(resource.imageResId)
                binding.ivPreview.visibility = View.VISIBLE
                binding.layoutImagePicker.visibility = View.GONE
                binding.btnRemoveImage.visibility = View.VISIBLE
            }

            if (resource.latitude != null && resource.longitude != null) {
                binding.tvGpsStatus.text = String.format(Locale.US, "GPS: %.4f, %.4f", resource.latitude, resource.longitude)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            cameraLauncher.launch(null)
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            existingImageUrl = ""
            binding.ivPreview.visibility = View.GONE
            binding.layoutImagePicker.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.GONE
        }

        binding.btnDetectGps.setOnClickListener {
            checkAndRequestLocation()
        }

        binding.btnSubmitResource.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val title = binding.etTitle.text?.toString() ?: ""
        val listingType = binding.actListingType.text?.toString() ?: ""
        val category = binding.actCategory.text?.toString() ?: ""
        val quantity = binding.etQuantity.text?.toString() ?: ""
        val location = binding.etLocation.text?.toString() ?: ""
        val description = binding.etDescription.text?.toString() ?: ""

        val validation = viewModel.validateForm(
            title = title,
            listingType = listingType,
            category = category,
            quantity = quantity,
            location = location,
            description = description
        )

        binding.tilTitle.error = validation.titleError
        binding.tilListingType.error = validation.listingTypeError
        binding.tilCategory.error = validation.categoryError
        binding.tilQuantity.error = validation.quantityError
        binding.tilLocation.error = validation.locationError
        binding.tilDescription.error = validation.descriptionError

        if (!validation.isValid) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent double submission
        binding.btnSubmitResource.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        viewModel.saveResource(
            existingId = existingResourceId,
            title = title,
            listingType = listingType,
            category = category,
            quantity = quantity,
            location = location,
            description = description,
            latitude = detectedLat,
            longitude = detectedLng,
            localImageUri = selectedImageUri,
            existingImageUrl = existingImageUrl
        )
    }

    private fun checkAndRequestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun fetchGpsLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Location is turned off. Please enable GPS in device settings.", Toast.LENGTH_LONG).show()
            return
        }

        binding.tvGpsStatus.text = "Acquiring GPS location..."
        binding.btnDetectGps.isEnabled = false

        locationCancellationTokenSource?.cancel()
        val cts = CancellationTokenSource()
        locationCancellationTokenSource = cts

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    binding.btnDetectGps.isEnabled = true
                    if (location != null) {
                        applyGpsLocation(location)
                    } else {
                        // Fallback to lastLocation if available
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                applyGpsLocation(lastLoc)
                            } else {
                                binding.tvGpsStatus.text = "Unable to determine location. Please enter manually."
                                Toast.makeText(this, "Unable to determine your location. Please enter it manually.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            binding.tvGpsStatus.text = "Location unavailable. Please enter manually."
                            Toast.makeText(this, "Unable to determine your location. Please enter it manually.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    binding.btnDetectGps.isEnabled = true
                    binding.tvGpsStatus.text = "GPS error: ${e.localizedMessage ?: "Unknown"}"
                    Toast.makeText(this, "Unable to determine your location. Please enter it manually.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            binding.btnDetectGps.isEnabled = true
            Toast.makeText(this, "Location permission issue: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyGpsLocation(location: Location) {
        detectedLat = location.latitude
        detectedLng = location.longitude

        lifecycleScope.launch(Dispatchers.IO) {
            var readableAddress = ""
            try {
                val geocoder = Geocoder(this@AddEditResourceActivity, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val parts = mutableListOf<String>()
                    val subLoc = address.subLocality ?: address.thoroughfare ?: address.featureName
                    val loc = address.locality ?: address.subAdminArea ?: address.adminArea
                    if (!subLoc.isNullOrBlank()) parts.add(subLoc)
                    if (!loc.isNullOrBlank() && loc != subLoc) parts.add(loc)

                    readableAddress = if (parts.isNotEmpty()) parts.joinToString(", ") else (address.getAddressLine(0) ?: "")
                }
            } catch (e: Exception) {
                // Reverse geocoding failed (e.g. offline)
            }

            withContext(Dispatchers.Main) {
                if (readableAddress.isNotBlank()) {
                    binding.etLocation.setText(readableAddress)
                    binding.tilLocation.error = null
                    binding.tvGpsStatus.text = String.format(Locale.US, "GPS: %s (%.4f, %.4f)", readableAddress, location.latitude, location.longitude)
                    Toast.makeText(this@AddEditResourceActivity, "Location: $readableAddress", Toast.LENGTH_SHORT).show()
                } else {
                    val fallback = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", location.latitude, location.longitude)
                    binding.etLocation.setText(fallback)
                    binding.tilLocation.error = null
                    binding.tvGpsStatus.text = "GPS: $fallback"
                    Toast.makeText(this@AddEditResourceActivity, "GPS coordinates captured!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun persistImageToInternalStorage(sourceUri: Uri): Uri {
        return try {
            val dir = File(filesDir, "resource_images").apply { mkdirs() }
            val file = File(dir, "res_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            sourceUri
        }
    }

    private fun persistBitmapToInternalStorage(bitmap: Bitmap): Uri {
        return try {
            val dir = File(filesDir, "resource_images").apply { mkdirs() }
            val file = File(dir, "res_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Uri.EMPTY
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.setImageURI(uri)
        binding.ivPreview.visibility = View.VISIBLE
        binding.layoutImagePicker.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.VISIBLE
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AddEditUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmitResource.isEnabled = false
                        binding.btnSubmitResource.text = state.message
                    }
                    is AddEditUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitResource.isEnabled = true
                        Toast.makeText(this@AddEditResourceActivity, "Resource shared successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is AddEditUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitResource.isEnabled = true
                        binding.btnSubmitResource.text = "Share Resource"
                        Toast.makeText(this@AddEditResourceActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is AddEditUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitResource.isEnabled = true
                        binding.btnSubmitResource.text = "Share Resource"
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCancellationTokenSource?.cancel()
    }
}
