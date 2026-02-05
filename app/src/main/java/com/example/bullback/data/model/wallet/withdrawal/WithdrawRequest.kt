package com.example.bullback.data.model.wallet.withdrawal

import com.google.gson.annotations.SerializedName

data class WithdrawRequest(
    val amount: String,

    @SerializedName("account_name")
    val accountName: String,

    @SerializedName("account_number")
    val accountNumber: String,

    @SerializedName("ifsc_code")
    val ifscCode: String,

    @SerializedName("upi_mobile")
    val upiMobile: String? = null
)
