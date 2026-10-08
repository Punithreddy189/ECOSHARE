package com.ecoshare.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ItemResourceBinding
import com.ecoshare.app.model.Resource

class ResourceAdapter(
    private val onItemClick: (Resource) -> Unit,
    private val onBookmarkClick: (Resource) -> Unit,
    private val isBookmarked: (Resource) -> Boolean
) : ListAdapter<Resource, ResourceAdapter.ResourceViewHolder>(ResourceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResourceViewHolder(private val binding: ItemResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resource: Resource) {
            binding.tvTitle.text = resource.title
            binding.tvCategory.text = resource.category
            binding.tvLocation.text = resource.location.ifBlank { "Community" }
            binding.tvEcoPoints.text = "🪙 +${resource.ecoPoints} pts"
            binding.tvPriceOrStatus.text = if (resource.price.isNotBlank() && resource.price.lowercase() != "free") resource.price else "Free"

            // Listing Type Badge
            val lType = if (resource.listingType.isNotBlank()) resource.listingType else "Borrow / Free"
            binding.tvTypeBadge.text = lType
            when {
                lType.contains("Rent", ignoreCase = true) -> {
                    binding.tvTypeBadge.setBackgroundResource(R.drawable.badge_pending)
                    binding.tvTypeBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.type_rent))
                }
                lType.contains("Sell", ignoreCase = true) -> {
                    binding.tvTypeBadge.setBackgroundResource(R.drawable.badge_available)
                    binding.tvTypeBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.type_sell))
                }
                lType.contains("Exchange", ignoreCase = true) -> {
                    binding.tvTypeBadge.setBackgroundResource(R.drawable.badge_shared)
                    binding.tvTypeBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.type_exchange))
                }
                else -> { // Borrow / Free
                    binding.tvTypeBadge.setBackgroundResource(R.drawable.badge_available)
                    binding.tvTypeBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryDark))
                }
            }

            // Image priority: 1. Local real photo/URL, 2. imageResId, 3. Category illustration fallback
            val fallbackIllustration = getCategoryFallback(resource.category)
            binding.ivResourceImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

            if (resource.imageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(resource.imageUrl)
                    .placeholder(R.drawable.bg_search_bar)
                    .error(fallbackIllustration)
                    .into(binding.ivResourceImage)
            } else if (resource.imageResId != 0) {
                binding.ivResourceImage.setImageResource(resource.imageResId)
            } else {
                binding.ivResourceImage.setImageResource(fallbackIllustration)
            }

            // Star / Favorite icon
            val bookmarked = isBookmarked(resource)
            if (bookmarked) {
                binding.btnBookmark.setImageResource(R.drawable.ic_star_filled)
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_star_outline)
            }

            binding.btnBookmark.setOnClickListener {
                onBookmarkClick(resource)
            }

            binding.root.setOnClickListener {
                onItemClick(resource)
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

    class ResourceDiffCallback : DiffUtil.ItemCallback<Resource>() {
        override fun areItemsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem.resourceId == newItem.resourceId
        }

        override fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem == newItem
        }
    }
}
