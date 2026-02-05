package com.example.bullback.ui.profile.changepassword

import com.example.bullback.data.model.changepassword.ChangePasswordRequest
import com.example.bullback.data.remote.api.ChangePasswordApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.bullback.data.remote.RetrofitClient
import kotlinx.coroutines.launch

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    data class Success(val message: String) : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
    data class ValidationError(val field: String, val message: String) : PasswordChangeState()
}

class ChangePasswordViewModel : ViewModel() {

    private val apiService: ChangePasswordApi by lazy {
        RetrofitClient.createService(ChangePasswordApi::class.java)
    }

    private val _passwordChangeState = MutableLiveData<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: LiveData<PasswordChangeState> = _passwordChangeState

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        // Validation
        val validationError = validatePasswords(oldPassword, newPassword, confirmPassword)
        if (validationError != null) {
            _passwordChangeState.value = validationError
            return
        }

        _passwordChangeState.value = PasswordChangeState.Loading

        val request = ChangePasswordRequest(
            oldPassword = oldPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )

        viewModelScope.launch {
            try {
                val response = apiService.changePassword(request)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.status) {
                        _passwordChangeState.value = PasswordChangeState.Success(result.message)
                    } else {
                        _passwordChangeState.value = PasswordChangeState.Error(result.message)
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid password or request"
                        401 -> "Current password is incorrect"
                        404 -> "User not found"
                        500 -> "Server error, please try again later"
                        else -> "Failed to change password"
                    }
                    _passwordChangeState.value = PasswordChangeState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _passwordChangeState.value = PasswordChangeState.Error(
                    "Network error: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    private fun validatePasswords(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): PasswordChangeState.ValidationError? {
        return when {
            oldPassword.isEmpty() -> {
                PasswordChangeState.ValidationError("old_password", "Please enter current password")
            }
            newPassword.isEmpty() -> {
                PasswordChangeState.ValidationError("new_password", "Please enter new password")
            }
            newPassword.length < 6 -> {
                PasswordChangeState.ValidationError("new_password", "Password must be at least 6 characters")
            }
            confirmPassword.isEmpty() -> {
                PasswordChangeState.ValidationError("confirm_password", "Please confirm your password")
            }
            newPassword != confirmPassword -> {
                PasswordChangeState.ValidationError("confirm_password", "Passwords do not match")
            }
            oldPassword == newPassword -> {
                PasswordChangeState.ValidationError("new_password", "New password must be different from old password")
            }
            else -> null
        }
    }

    fun resetState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }
}