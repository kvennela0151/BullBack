package com.example.bullback.data.model.wallet.deposit

import com.google.gson.annotations.SerializedName

data class ActiveBankAccountResponse(
    val status: Boolean,
    val message: String,
    val data: BankAccount
)

data class BankAccount(
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("ifscCode") val ifscCode: String,
    @SerializedName("upi") val upi: String?,
    @SerializedName("amtAfterChangeUpi") val amtAfterChangeUpi: Int,
    @SerializedName("bankAccountId") val bankAccountId: Int? = null  // if backend sends
)
