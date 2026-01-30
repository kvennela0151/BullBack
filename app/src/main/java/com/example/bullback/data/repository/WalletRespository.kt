package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.BankAccount
import com.example.bullback.data.model.wallet.WalletTransaction
import com.example.bullback.data.remote.api.WalletApiService

class WalletRepository(
    private val api: WalletApiService
) {

    suspend fun getTransactions(type: String): List<WalletTransaction> {
        return api.getTransactions(type).data.items
    }
}
