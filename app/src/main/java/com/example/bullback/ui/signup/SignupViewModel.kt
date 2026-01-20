package com.example.bullback.ui.signup


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.repository.AuthRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application)

    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    private var usernameCheckJob: Job? = null

    fun onFullNameChange(fullName: String) {
        _state.update { it.copy(fullName = fullName) }
    }

    fun onUsernameChange(username: String) {
        _state.update { it.copy(username = username, usernameChecked = false, isUsernameAvailable = false) }

        // Debounce username checking
        usernameCheckJob?.cancel()
        usernameCheckJob = viewModelScope.launch {
            delay(500) // Wait for 500ms after last keystroke
            if (username.length >= 3) {
                checkUsernameAvailability(username)
            }
        }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPhoneChange(phone: String) {
        _state.update { it.copy(phone = phone) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onReferralCodeChange(referralCode: String) {
        _state.update { it.copy(referralCode = referralCode) }
    }

    fun signup() {
        if (validateInput()) {
            _state.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            viewModelScope.launch {
                val result = authRepository.signup(
                    fullName = _state.value.fullName,
                    username = _state.value.username,
                    email = _state.value.email,
                    phone = _state.value.phone,
                    password = _state.value.password,
                    referralCode = _state.value.referralCode.ifBlank { null }
                )

                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun checkUsernameAvailability(username: String) {
        viewModelScope.launch {
            val result = authRepository.checkUsername(username)

            when (result) {
                is Resource.Success -> {
                    // Assuming the result data has 'available' and 'message' properties
                    _state.update {
                        it.copy(
                            usernameChecked = true,
                            isUsernameAvailable = result.data.available,
                            usernameCheckMessage = result.data.message
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            usernameChecked = true,
                            isUsernameAvailable = false,
                            usernameCheckMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                    _state.update { it.copy(usernameChecked = false) }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = _state.value.fullName
        val username = _state.value.username
        val email = _state.value.email
        val phone = _state.value.phone
        val password = _state.value.password

        if (fullName.isBlank()) {
            _state.update { it.copy(error = "Full name is required") }
            return false
        }

        if (username.isBlank()) {
            _state.update { it.copy(error = "Username is required") }
            return false
        }

        if (username.length < 3) {
            _state.update { it.copy(error = "Username must be at least 3 characters") }
            return false
        }

        if (!_state.value.usernameChecked) {
            _state.update { it.copy(error = "Please wait for username check") }
            return false
        }

        if (!_state.value.isUsernameAvailable) {
            _state.update { it.copy(error = "Username is not available") }
            return false
        }

        if (email.isBlank()) {
            _state.update { it.copy(error = "Email is required") }
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(error = "Enter a valid email address") }
            return false
        }

        if (phone.isBlank()) {
            _state.update { it.copy(error = "Phone number is required") }
            return false
        }

        if (password.isBlank()) {
            _state.update { it.copy(error = "Password is required") }
            return false
        }

        if (password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return false
        }

        return true
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}


