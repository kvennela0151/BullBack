package com.example.bullback.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.User
import com.example.bullback.data.model.auth.UserMeResponse
import com.example.bullback.data.repository.AuthRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadProfile() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = authRepository.getProfile()) {

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            error = null
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class ProfileState(
    val user: UserMeResponse? = null,  // changed from User?
    val isLoading: Boolean = false,
    val error: String? = null
)

