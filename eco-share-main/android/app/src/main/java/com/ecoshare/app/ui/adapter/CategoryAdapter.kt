package com.ecoshare.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ItemCategoryChipBinding

class CategoryAdapter(
    private val categories: List<String>,
    private var selectedCategory: String,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category.equals(selectedCategory, ignoreCase = true))
    }

    override fun getItemCount(): Int = categories.size

    fun setSelectedCategory(category: String) {
        selectedCategory = category
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, isSelected: Boolean) {
            binding.tvCategoryChip.text = category
            if (isSelected) {
                binding.tvCategoryChip.setBackgroundResource(R.drawable.badge_available)
                binding.tvCategoryChip.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimaryDark))
            } else {
                binding.tvCategoryChip.setBackgroundResource(R.drawable.badge_completed)
                binding.tvCategoryChip.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
            }

            binding.root.setOnClickListener {
                onCategorySelected(category)
            }
        }
    }
}
