package com.example.bullback.ui.profile.tradelogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.TradeRepository

class TradeLogsVMFactory(
    private val repository: TradeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradeLogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradeLogsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
