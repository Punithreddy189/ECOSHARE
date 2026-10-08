package com.ecoshare.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.databinding.ActivityRegisterBinding
import com.ecoshare.app.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvGoToLogin.setOnClickListener { finish() }

        // Clear field errors as user types
        binding.etName.doAfterTextChanged {
            binding.tilName.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }
        binding.etLocation.doAfterTextChanged {
            binding.tilLocation.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }
        binding.etConfirmPassword.doAfterTextChanged {
            binding.tilConfirmPassword.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val pass = binding.etPassword.text.toString()
            val confirmPass = binding.etConfirmPassword.text.toString()

            var isValid = true

            // 1. Name validation
            if (name.isBlank()) {
                binding.tilName.error = "Full Name is required."
                isValid = false
            } else if (name.length < 2) {
                binding.tilName.error = "Name must be at least 2 characters."
                isValid = false
            } else {
                binding.tilName.error = null
            }

            // 2. Email validation
            if (email.isBlank()) {
                binding.tilEmail.error = "Email Address is required."
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid email address."
                isValid = false
            } else {
                binding.tilEmail.error = null
            }

            // 3. Location / Neighborhood validation
            if (location.isBlank()) {
                binding.tilLocation.error = "Location / Neighborhood is required."
                isValid = false
            } else {
                binding.tilLocation.error = null
            }

            // 4. Password validation
            if (pass.isBlank()) {
                binding.tilPassword.error = "Password is required."
                isValid = false
            } else if (pass.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters."
                isValid = false
            } else {
                binding.tilPassword.error = null
            }

            // 5. Confirm Password validation
            if (confirmPass.isBlank()) {
                binding.tilConfirmPassword.error = "Please confirm your password."
                isValid = false
            } else if (pass != confirmPass) {
                binding.tilConfirmPassword.error = "Passwords do not match."
                isValid = false
            } else {
                binding.tilConfirmPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            viewModel.register(email, pass, name, location)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSignUp.isEnabled = false
                        binding.btnSignUp.alpha = 0.6f
                        binding.tvErrorBanner.visibility = View.GONE
                    }
                    is AuthUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled = true
                        binding.btnSignUp.alpha = 1.0f
                        Toast.makeText(this@RegisterActivity, "Account created successfully! Welcome to EcoShare.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is AuthUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled = true
                        binding.btnSignUp.alpha = 1.0f
                        binding.tvErrorBanner.text = state.message
                        binding.tvErrorBanner.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled = true
                        binding.btnSignUp.alpha = 1.0f
                        binding.tvErrorBanner.visibility = View.GONE
                    }
                }
            }
        }
    }
}
