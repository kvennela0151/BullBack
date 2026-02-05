package com.example.bullback.data.model.wallet.walletbalance

data class WalletBalanceResponse(

    val status: Boolean,
    val message: String,
    val data: WalletBalanceData?
)
