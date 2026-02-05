package com.example.bullback.ui.profile.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.PositionsRepository
import com.example.bullback.data.repository.WalletRepository

class WalletViewModelFactory(
    private val walletRepo: WalletRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            return WalletViewModel(walletRepo) as T   // positionsRepo auto uses default value!
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
