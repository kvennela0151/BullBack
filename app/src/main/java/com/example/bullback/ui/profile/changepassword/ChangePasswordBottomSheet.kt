package com.example.bullback.ui.profile.changepassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bullback.data.model.changepassword.ChangePasswordRequest
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.ChangePasswordApi
import com.example.bullback.databinding.FragmentChangePasswordBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class ChangePasswordBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentChangePasswordBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ChangePasswordApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize API Service
        apiService = RetrofitClient.createService(ChangePasswordApi::class.java)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnProceed.setOnClickListener {
            validateAndChangePassword()
        }
    }

    private fun validateAndChangePassword() {
        // Clear previous errors
        binding.tilOldPassword.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null

        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation
        var isValid = true

        if (oldPassword.isEmpty()) {
            binding.tilOldPassword.error = "Please enter current password"
            isValid = false
        }

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "Please enter new password"
            isValid = false
        } else if (newPassword.length < 6) {
            binding.tilNewPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (oldPassword.isNotEmpty() && oldPassword == newPassword) {
            binding.tilNewPassword.error = "New password must be different from old password"
            isValid = false
        }

        if (!isValid) return

        // Make API call
        changePassword(oldPassword, newPassword, confirmPassword)
    }

    private fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        showLoading(true)

        val request = ChangePasswordRequest(
            oldPassword = oldPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )

        lifecycleScope.launch {
            try {
                val response = apiService.changePassword(request)

                showLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status) {
                        Toast.makeText(
                            requireContext(),
                            result.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Close the bottom sheet
                        dismiss()

                        // Optional: Logout user or navigate to login
                        // (requireActivity() as? MainActivity)?.logoutUser()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            result.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle error response
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid password or request"
                        401 -> "Current password is incorrect"
                        404 -> "User not found"
                        500 -> "Server error, please try again later"
                        else -> "Failed to change password"
                    }

                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnProceed.isEnabled = !isLoading
        binding.etOldPassword.isEnabled = !isLoading
        binding.etNewPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ChangePasswordBottomSheet"

        fun newInstance() = ChangePasswordBottomSheet()
    }
}