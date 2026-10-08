package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ecoshare.app.R
import com.ecoshare.app.databinding.FragmentBrowseBinding
import com.ecoshare.app.ui.adapter.CategoryAdapter
import com.ecoshare.app.ui.adapter.ResourceAdapter
import com.ecoshare.app.ui.resource.ResourceDetailActivity

class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var resourceAdapter: ResourceAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val categories = listOf(
        "All", "Tools & Equipment", "Food", "Clothes", "Books", "Furniture",
        "Electronics", "Kitchen Items", "Garden & Outdoor", "Household Items", "Other"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("EcoShare", "BrowseFragment loaded")
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // 2-column Grid for Listings
        resourceAdapter = ResourceAdapter(
            onItemClick = { resource ->
                val intent = Intent(requireContext(), ResourceDetailActivity::class.java).apply {
                    putExtra("EXTRA_RESOURCE", resource)
                }
                startActivity(intent)
            },
            onBookmarkClick = { resource ->
                viewModel.toggleBookmark(resource.resourceId)
            },
            isBookmarked = { resource ->
                viewModel.currentUser.value?.savedResources?.contains(resource.resourceId) == true
            }
        )
        binding.rvResources.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvResources.adapter = resourceAdapter

        // Category chips adapter
        categoryAdapter = CategoryAdapter(
            categories = categories,
            selectedCategory = viewModel.selectedCategory.value,
            onCategorySelected = { category ->
                viewModel.selectedCategory.value = category
                categoryAdapter.setSelectedCategory(category)
            }
        )
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupListeners() {
        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuery.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Listing Types chip filter
        binding.chipGroupTypes.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            if (checkedId == R.id.chipTypeMyShares) {
                viewModel.selectedTab.value = ResourceTab.MY_SHARES
                viewModel.selectedListingType.value = "All Types"
            } else {
                viewModel.selectedTab.value = ResourceTab.RECENT
                val selectedType = when (checkedId) {
                    R.id.chipTypeBorrow -> "Borrow / Free"
                    R.id.chipTypeRent -> "Rent"
                    R.id.chipTypeExchange -> "Exchange"
                    R.id.chipTypeSell -> "Sell"
                    else -> "All Types"
                }
                viewModel.selectedListingType.value = selectedType
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.filteredResources.collect { list ->
                resourceAdapter.submitList(list)
                binding.layoutEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvResources.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.currentUser.collect {
                resourceAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
