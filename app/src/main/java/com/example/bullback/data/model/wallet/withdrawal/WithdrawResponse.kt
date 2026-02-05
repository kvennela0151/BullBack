package com.example.bullback.data.model.wallet.withdrawal

import com.google.gson.annotations.SerializedName

data class WithdrawResponse(
    val id: Int,
    val amount: String,
    val status: String,

    @SerializedName("reference_id")
    val referenceId: String?,

    @SerializedName("account_name")
    val accountName: String,

    @SerializedName("account_number")
    val accountNumber: String,

    @SerializedName("ifsc_code")
    val ifscCode: String,

    @SerializedName("upi_mobile")
    val upiMobile: String?,

    @SerializedName("created_at")
    val createdAt: String
)
