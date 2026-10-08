package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ecoshare.app.R
import com.ecoshare.app.databinding.FragmentHomeBinding
import com.ecoshare.app.model.Resource
import com.ecoshare.app.ui.adapter.CategoryAdapter
import com.ecoshare.app.ui.adapter.ResourceAdapter
import com.ecoshare.app.ui.resource.AddEditResourceActivity
import com.ecoshare.app.ui.resource.ResourceDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var resourceAdapter: ResourceAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val categories = listOf(
        "All", "Tools & Equipment", "Food", "Clothes", "Books", "Furniture",
        "Electronics", "Kitchen Items", "Garden & Outdoor", "Household Items", "Other"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("EcoShare", "HomeFragment loaded")
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
        binding.btnViewAll.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav?.selectedItemId = R.id.nav_browse
        }

        binding.btnEmptyShare.setOnClickListener {
            val intent = Intent(requireContext(), AddEditResourceActivity::class.java)
            startActivity(intent)
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        // Community Eco Impact Stats
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.communityImpact.collect { (items, co2, neighbors) ->
                binding.tvStatItemsShared.text = if (items > 0) "${items}+" else "0"
                binding.tvStatCo2Offset.text = String.format("%.0f kg", co2)
                binding.tvStatNeighbors.text = neighbors.toString()
            }
        }

        // 2-Column Listing items
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
