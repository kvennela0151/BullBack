package com.example.bullback.ui.profile.marginSettings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.MarginUiModel
import com.example.bullback.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MarginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarginRepository =
        MarginRepository(
            AuthRepository.getInstance(application.applicationContext)
        )

    private val _marginSettings = MutableStateFlow<List<MarginUiModel>>(emptyList())
    val marginSettings: StateFlow<List<MarginUiModel>> = _marginSettings

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMarginSettings() {
        viewModelScope.launch {
            repository.getMarginSettings()
                .onSuccess { _marginSettings.value = it }
                .onFailure { _error.value = it.message }
        }
    }
}