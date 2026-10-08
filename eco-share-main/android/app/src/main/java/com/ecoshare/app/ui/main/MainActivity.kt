package com.ecoshare.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ActivityMainBinding
import com.ecoshare.app.ui.resource.AddEditResourceActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        android.util.Log.d("EcoShare", "MainActivity onCreate started")
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            android.util.Log.d("EcoShare", "Loading HomeFragment as initial destination")
            replaceFragment(HomeFragment(), "EcoShare")
        }

        setupBottomNavigation()
        setupFab()
        setupHeader()
        observeCurrentUser()
    }

    private fun setupHeader() {
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "All caught up! No unread community notifications.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeCurrentUser() {
        lifecycleScope.launchWhenStarted {
            viewModel.currentUser.collect { user ->
                if (user != null) {
                    val pts = if (user.ecoPoints > 0) user.ecoPoints else 150
                    binding.tvHeaderPoints.text = "🪙 $pts"
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), "EcoShare")
                    binding.fabAddResource.show()
                    true
                }
                R.id.nav_browse -> {
                    replaceFragment(BrowseFragment(), "Browse Community")
                    binding.fabAddResource.show()
                    true
                }
                R.id.nav_events -> {
                    replaceFragment(EventsFragment(), "Community Events")
                    binding.fabAddResource.hide()
                    true
                }
                R.id.nav_messages -> {
                    replaceFragment(MessagesFragment(), "Messages")
                    binding.fabAddResource.hide()
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment(), "My Profile")
                    binding.fabAddResource.hide()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        binding.fabAddResource.setOnClickListener {
            val intent = Intent(this, AddEditResourceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        binding.tvAppTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
