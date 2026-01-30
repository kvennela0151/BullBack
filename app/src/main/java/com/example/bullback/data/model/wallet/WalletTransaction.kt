package com.example.bullback.data.model.wallet

import com.google.gson.annotations.SerializedName

data class WalletTransaction(
    val id: Int,
    val amount: String,
    val type: String,
    val status: String,

    @SerializedName("admin_remark")
    val adminRemark: String?,

    @SerializedName("created_at")
    val createdAt: String
)
