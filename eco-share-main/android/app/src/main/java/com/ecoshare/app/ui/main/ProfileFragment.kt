package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.R
import com.ecoshare.app.databinding.FragmentProfileBinding
import com.ecoshare.app.ui.auth.LoginActivity
import com.ecoshare.app.ui.profile.EditProfileActivity
import com.ecoshare.app.util.ThemeHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("EcoShare", "ProfileFragment loaded")

        updateThemeModeDisplay()

        val openEdit = {
            val user = viewModel.currentUser.value
            if (user != null) {
                val intent = Intent(requireContext(), EditProfileActivity::class.java).apply {
                    putExtra("EXTRA_NAME", user.displayName)
                    putExtra("EXTRA_EMAIL", user.email)
                    putExtra("EXTRA_LOCATION", user.location)
                    putExtra("EXTRA_IMAGE_URL", user.profileImageUrl)
                }
                startActivity(intent)
            }
        }

        binding.btnEditProfile.setOnClickListener { openEdit() }
        binding.rowEditProfile.setOnClickListener { openEdit() }

        binding.rowRewards.setOnClickListener {
            val pts = viewModel.currentUser.value?.ecoPoints ?: 150
            Toast.makeText(requireContext(), "You have $pts EcoPoints! Redeem for community badges & tree planting.", Toast.LENGTH_LONG).show()
        }

        binding.colSharedListings.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
            viewModel.selectedTab.value = ResourceTab.MY_SHARES
            bottomNav?.selectedItemId = R.id.nav_browse
        }

        binding.rowAppearance.setOnClickListener {
            showAppearanceDialog()
        }

        binding.rowNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Push notifications and community activity alerts are enabled.", Toast.LENGTH_SHORT).show()
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.currentUser.collect { user ->
                if (user != null) {
                    binding.tvProfileName.text = user.displayName
                    binding.tvProfileEmail.text = user.email
                    binding.tvProfileLocation.text = user.location
                    binding.tvProfileAvatar.text = user.displayName.take(1).ifBlank { "P" }.uppercase()
                    binding.tvProfilePoints.text = (if (user.ecoPoints > 0) user.ecoPoints else 150).toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.allResources.collect { resources ->
                val currentUid = viewModel.currentUser.value?.uid ?: return@collect
                val userShares = resources.filter { it.ownerId == currentUid }
                val count = userShares.size
                val co2 = userShares.sumOf { it.co2Offset }

                binding.tvSharedCount.text = count.toString()
                binding.tvCo2Saved.text = String.format("%.1f kg", if (co2 > 0) co2 else (count * 2.5))

                // Ranks: Lvl 1 (0-2), Lvl 2 (3-4), Lvl 3 (5-9), Lvl 4 (10+)
                val (badge, title, lvl, nextTarget, prevTarget) = when {
                    count < 3 -> Quintuple("🌱", "Eco Seedling", "Lvl 1", 3, 0)
                    count < 5 -> Quintuple("🌿", "Green Sprout", "Lvl 2", 5, 3)
                    count < 10 -> Quintuple("🌳", "Forest Guardian", "Lvl 3", 10, 5)
                    else -> Quintuple("👑", "Zero Waste Legend", "Lvl 4", count, 0)
                }

                binding.tvBadgeIcon.text = badge
                binding.tvBadgeTitle.text = title
                binding.tvLevelLabel.text = lvl
                binding.tvProgressText.text = "$count / $nextTarget Shares"

                val progress = if (nextTarget > prevTarget) {
                    (((count - prevTarget).toFloat() / (nextTarget - prevTarget)) * 100).toInt().coerceIn(0, 100)
                } else 100
                binding.pbRankProgress.progress = progress

                // Medals opacity
                binding.medal1.alpha = if (count >= 1) 1.0f else 0.3f
                binding.medal2.alpha = if (co2 >= 10.0 || count >= 2) 1.0f else 0.3f
                binding.medal3.alpha = if (count >= 5) 1.0f else 0.3f
                binding.medal4.alpha = if (co2 >= 50.0 || count >= 10) 1.0f else 0.3f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUserProfile()
        updateThemeModeDisplay()
    }

    private fun updateThemeModeDisplay() {
        if (_binding != null) {
            val mode = ThemeHelper.getThemeMode(requireContext())
            binding.tvCurrentThemeMode.text = ThemeHelper.getThemeModeLabel(mode)
        }
    }

    private fun showAppearanceDialog() {
        val options = arrayOf("System default", "Light", "Dark")
        val currentMode = ThemeHelper.getThemeMode(requireContext())
        val checkedIndex = when (currentMode) {
            ThemeHelper.MODE_LIGHT -> 1
            ThemeHelper.MODE_DARK -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Appearance & Theme")
            .setSingleChoiceItems(options, checkedIndex) { dialog, which ->
                val newMode = when (which) {
                    1 -> ThemeHelper.MODE_LIGHT
                    2 -> ThemeHelper.MODE_DARK
                    else -> ThemeHelper.MODE_SYSTEM
                }
                ThemeHelper.setThemeMode(requireContext(), newMode)
                updateThemeModeDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class Quintuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
