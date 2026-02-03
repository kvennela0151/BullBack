package com.example.bullback.data.model.wallet.depositwithdrawal

import com.google.gson.annotations.SerializedName

data class BankAccountData(

    val upi: String,

    @SerializedName("upi_mobile_number")
    val upiMobileNumber: String,

    @SerializedName("account_name")
    val accountName: String,

    @SerializedName("account_number")
    val accountNumber: String,

    @SerializedName("ifsc_code")
    val ifscCode: String,

    @SerializedName("amt_after_change_upi")
    val amtAfterChangeUpi: Double,

    @SerializedName("is_active")
    val isActive: Boolean,
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("total_deposited")
    val totalReceived: Double,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?
)
