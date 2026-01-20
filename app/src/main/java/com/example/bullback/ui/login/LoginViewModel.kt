package com.example.bullback.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.repository.AuthRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application)

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onUsernameChange(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun login() {
        if (validateInput()) {
            _state.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            viewModelScope.launch {
                val result = authRepository.login(
                    _state.value.username,
                    _state.value.password
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

    fun demoLogin() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.demoLogin()

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

    private fun validateInput(): Boolean {
        val username = _state.value.username
        val password = _state.value.password

        if (username.isBlank()) {
            _state.update { it.copy(error = "Username is required") }
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


