package com.example.bullback.ui.profile.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.WalletRepository

class WalletViewModelFactory(
    private val repository: WalletRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WalletViewModel(repository) as T
    }
}
