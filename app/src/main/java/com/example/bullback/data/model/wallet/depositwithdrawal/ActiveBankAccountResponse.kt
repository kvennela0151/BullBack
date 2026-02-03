package com.example.bullback.data.model.wallet.depositwithdrawal

data class ActiveBankAccountResponse(

    val status: Boolean,
    val message: String?,
    val data: BankAccountData?
)
