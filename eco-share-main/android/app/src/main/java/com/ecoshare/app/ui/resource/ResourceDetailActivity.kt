package com.ecoshare.app.ui.resource

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ActivityResourceDetailBinding
import com.ecoshare.app.model.Resource
import com.ecoshare.app.ui.chat.ChatActivity

class ResourceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResourceDetailBinding
    private val viewModel: ResourceDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resource = intent.getSerializableExtra("EXTRA_RESOURCE") as? Resource
        if (resource != null) {
            viewModel.setResource(resource)
        } else {
            val resourceId = intent.getStringExtra("EXTRA_RESOURCE_ID")
            if (resourceId != null) {
                viewModel.loadResource(resourceId)
            } else {
                finish()
                return
            }
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnBookmark.setOnClickListener {
            viewModel.toggleBookmark()
        }

        binding.btnMessageOwner.setOnClickListener {
            viewModel.startChat()
        }

        binding.btnRequestResource.setOnClickListener {
            viewModel.requestResource()
            Toast.makeText(this, "Item requested! Status updated to Pending.", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditListing.setOnClickListener {
            val res = viewModel.resource.value ?: return@setOnClickListener
            val intent = Intent(this, AddEditResourceActivity::class.java).apply {
                putExtra("EXTRA_RESOURCE", res)
            }
            startActivity(intent)
        }

        binding.btnDeleteListing.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this shared item? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteResource()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.resource.collect { res ->
                if (res != null) {
                    populateUi(res)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.currentUser.collect { user ->
                val res = viewModel.resource.value
                if (res != null && user != null) {
                    val isOwner = res.ownerId == user.uid
                    binding.layoutOwnerControls.visibility = if (isOwner) View.VISIBLE else View.GONE
                    binding.layoutNonOwnerBar.visibility = if (isOwner) View.GONE else View.VISIBLE

                    val isSaved = user.savedResources.contains(res.resourceId)
                    binding.btnBookmark.setImageResource(
                        if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
                    )
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DetailUiState.Deleted -> {
                        Toast.makeText(this@ResourceDetailActivity, "Listing deleted successfully.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is DetailUiState.ChatStarted -> {
                        val intent = Intent(this@ResourceDetailActivity, ChatActivity::class.java).apply {
                            putExtra("EXTRA_CHAT", state.chat)
                        }
                        startActivity(intent)
                    }
                    is DetailUiState.Error -> {
                        Toast.makeText(this@ResourceDetailActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun populateUi(res: Resource) {
        binding.tvDetailTitle.text = res.title
        binding.tvDetailCategory.text = res.category
        binding.tvDetailQuantity.text = res.quantity
        binding.tvDetailLocation.text = res.location
        binding.tvDetailDescription.text = res.description
        binding.tvDetailOwner.text = "Posted by ${res.ownerName}"
        binding.tvDetailDate.text = "Posted ${res.createdAt.take(10)}"

        // Image priority: 1. Local real photo/URL, 2. imageResId, 3. Category illustration fallback
        val fallbackIllustration = getCategoryFallback(res.category)
        binding.ivDetailImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

        if (res.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(res.imageUrl)
                .placeholder(R.drawable.bg_search_bar)
                .error(fallbackIllustration)
                .into(binding.ivDetailImage)
        } else if (res.imageResId != 0) {
            binding.ivDetailImage.setImageResource(res.imageResId)
        } else {
            binding.ivDetailImage.setImageResource(fallbackIllustration)
        }

        // Badges
        binding.tvStatusBadge.text = res.status
        binding.tvTypeBadge.text = res.listingType

        // Owner status spinner
        val statuses = listOf("Available", "Pending", "Shared", "Completed")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        binding.spnStatusChanger.adapter = adapter
        val currentIndex = statuses.indexOf(res.status).coerceAtLeast(0)
        binding.spnStatusChanger.setSelection(currentIndex)

        binding.spnStatusChanger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selected = statuses[pos]
                if (selected != res.status) {
                    viewModel.updateStatus(selected)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getCategoryFallback(category: String): Int {
        return when {
            category.contains("Tool", ignoreCase = true) -> R.drawable.demo_drill
            category.contains("Food", ignoreCase = true) || category.contains("Produce", ignoreCase = true) -> R.drawable.demo_lemons
            category.contains("Sport", ignoreCase = true) || category.contains("Outdoor", ignoreCase = true) -> R.drawable.demo_tent
            category.contains("Transport", ignoreCase = true) || category.contains("Bike", ignoreCase = true) -> R.drawable.demo_bicycle
            category.contains("Book", ignoreCase = true) -> R.drawable.demo_books
            category.contains("Garden", ignoreCase = true) -> R.drawable.demo_lawn_mower
            else -> R.drawable.demo_drill
        }
    }
}
