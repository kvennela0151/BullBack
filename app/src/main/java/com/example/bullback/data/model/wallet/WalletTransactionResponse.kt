package com.example.bullback.data.model.wallet

data class WalletTransactionResponse(
    val status: Boolean,
    val message: String,
    val data: WalletTransactionData
)
