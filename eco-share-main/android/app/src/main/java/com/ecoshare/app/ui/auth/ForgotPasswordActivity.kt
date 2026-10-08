package com.ecoshare.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.ecoshare.app.R
import com.ecoshare.app.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
            binding.tvStatusBanner.visibility = View.GONE
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isBlank()) {
                binding.tilEmail.error = "Email address is required."
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid email address."
                return@setOnClickListener
            }

            viewModel.forgotPassword(email)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnResetPassword.isEnabled = false
                        binding.btnResetPassword.alpha = 0.6f
                        binding.tvStatusBanner.visibility = View.GONE
                    }
                    is AuthUiState.PasswordResetSent -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnResetPassword.isEnabled = true
                        binding.btnResetPassword.alpha = 1.0f
                        binding.tvStatusBanner.text = "Password reset email sent. Please check your inbox."
                        binding.tvStatusBanner.setBackgroundResource(R.drawable.bg_success_banner)
                        binding.tvStatusBanner.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.colorPrimaryDark))
                        binding.tvStatusBanner.visibility = View.VISIBLE
                    }
                    is AuthUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnResetPassword.isEnabled = true
                        binding.btnResetPassword.alpha = 1.0f
                        binding.tvStatusBanner.text = state.message
                        binding.tvStatusBanner.setBackgroundResource(R.drawable.bg_error_banner)
                        binding.tvStatusBanner.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.danger))
                        binding.tvStatusBanner.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnResetPassword.isEnabled = true
                        binding.btnResetPassword.alpha = 1.0f
                    }
                }
            }
        }
    }
}
