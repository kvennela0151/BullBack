package com.example.bullback.ui.positions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.PositionsRepository

class PositionsViewModelFactory(
    private val repository: PositionsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PositionsViewModel(repository) as T
    }
}
