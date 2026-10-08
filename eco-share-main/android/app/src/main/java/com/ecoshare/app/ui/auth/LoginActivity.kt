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
import com.ecoshare.app.databinding.ActivityLoginBinding
import com.ecoshare.app.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
            binding.tvErrorBanner.visibility = View.GONE
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString()

            var isValid = true
            if (email.isBlank()) {
                binding.tilEmail.error = "Email address is required."
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid email address."
                isValid = false
            } else {
                binding.tilEmail.error = null
            }

            if (pass.isBlank()) {
                binding.tilPassword.error = "Password is required."
                isValid = false
            } else {
                binding.tilPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            viewModel.login(email, pass)
        }

        binding.btnQuickDemo.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSignIn.isEnabled = false
                        binding.btnSignIn.alpha = 0.6f
                        binding.tvErrorBanner.visibility = View.GONE
                    }
                    is AuthUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignIn.isEnabled = true
                        binding.btnSignIn.alpha = 1.0f
                        Toast.makeText(this@LoginActivity, "Welcome back, ${state.user.displayName}!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is AuthUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignIn.isEnabled = true
                        binding.btnSignIn.alpha = 1.0f
                        binding.tvErrorBanner.text = state.message
                        binding.tvErrorBanner.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignIn.isEnabled = true
                        binding.btnSignIn.alpha = 1.0f
                        binding.tvErrorBanner.visibility = View.GONE
                    }
                }
            }
        }
    }
}
