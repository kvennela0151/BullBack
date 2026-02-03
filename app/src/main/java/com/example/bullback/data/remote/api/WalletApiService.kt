package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.ApiResponse
import com.example.bullback.data.model.auth.BankAccount
import com.example.bullback.data.model.wallet.WalletTransactionResponse
import com.example.bullback.data.model.wallet.depositwithdrawal.ActiveBankAccountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletApiService {

    @GET("api/v1/wallet/transactions")
    suspend fun getTransactions(
        @Query("type") type: String
    ): WalletTransactionResponse

    @GET("/api/v1/bank-accounts/active")
    suspend fun getActiveBankAccount(): Response<ActiveBankAccountResponse>

}
