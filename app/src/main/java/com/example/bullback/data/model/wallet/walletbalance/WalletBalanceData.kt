package com.example.bullback.data.model.wallet.walletbalance

import com.google.gson.annotations.SerializedName

data class WalletBalanceData(
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,
    val balance: String,
    val currency: String
)
