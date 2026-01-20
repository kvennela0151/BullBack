package com.example.bullback.ui.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bullback.MainActivity
import com.example.bullback.databinding.FragmentSignupBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.fragment.app.viewModels

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Back button
        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Sign up button
        binding.bntSignup.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhonenumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val referralCode = binding.etReferralCode.text.toString().trim()

            // Update view model
            viewModel.onFullNameChange(fullName)
            viewModel.onUsernameChange(username)
            viewModel.onEmailChange(email)
            viewModel.onPhoneChange(phone)
            viewModel.onPasswordChange(password)
            viewModel.onReferralCodeChange(referralCode)

            // Trigger signup
            viewModel.signup()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: SignupState) {
        // Loading state
        binding.bntSignup.isEnabled = !state.isLoading
        binding.bntSignup.text = if (state.isLoading) "Creating account..." else "SIGN UP"

        // Show/hide progress bar (add in XML first)
        // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Username availability check (optional - add TextView in XML)
        if (state.usernameChecked) {
            // Example: binding.tvUsernameStatus.text = if (state.isUsernameAvailable) "✓ Available" else "✗ Not available"
        }

        // Error handling
        state.error?.let { error ->
            showSnackbar(error)
            viewModel.clearError()
        }

        // Success handling
        if (state.isSuccess) {
            showToast("Account created successfully!")
            goBackToLogin()
        }
    }

    private fun goBackToLogin() {
        // Go back to previous fragment (LoginFragment)
        requireActivity().onBackPressed()

        // OR show success message
        // showToast("Signup successful! Please login.")
        // requireActivity().onBackPressed()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}