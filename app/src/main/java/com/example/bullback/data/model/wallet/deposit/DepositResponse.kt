package com.example.bullback.data.model.wallet.deposit

import com.google.gson.annotations.SerializedName

data class DepositResponse(
    val amount: String,
    val type: String,
    @SerializedName("reference_id")
    val referenceId: String?,
    @SerializedName("admin_remark")
    val adminRemark: String?,
    @SerializedName("bank_account_id")
    val bankAccountId: Int,
    @SerializedName("upi_mobile") val upiMobile: String?,
    @SerializedName("account_name") val accountName: String?,
    @SerializedName("account_number") val accountNumber: String?,
    @SerializedName("ifsc_code") val ifscCode: String?,
    val id: Int,
    @SerializedName("wallet_id") val walletId: Int,
    val status: String,
    @SerializedName("screenshot_url") val screenshotUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    val username: String?,
    @SerializedName("broker_id") val brokerId: String?,
    @SerializedName("sub_broker") val subBroker: String?
)
