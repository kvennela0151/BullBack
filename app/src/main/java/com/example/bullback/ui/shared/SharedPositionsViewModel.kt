package com.example.bullback.ui.shared


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.PositionsRepository
import com.example.bullback.ui.positions.PositionsViewModel

class SharedPositionsViewModel(private val repository: PositionsRepository) :
    PositionsViewModel(repository)

class SharedPositionsViewModelFactory(
    private val repository: PositionsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SharedPositionsViewModel(repository) as T
    }
}
