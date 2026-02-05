package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.ApiResponse
import com.example.bullback.data.model.auth.BankAccount
import com.example.bullback.data.model.wallet.WalletTransactionResponse
import com.example.bullback.data.model.wallet.deposit.DepositResponse
import com.example.bullback.data.model.wallet.depositwithdrawal.ActiveBankAccountResponse
import com.example.bullback.data.model.wallet.walletbalance.WalletBalanceResponse
import com.example.bullback.data.model.wallet.withdrawal.WithdrawResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface WalletApiService {

    @GET("api/v1/wallet/transactions")
    suspend fun getTransactions(
        @Query("type") type: String
    ): WalletTransactionResponse

    @GET("/api/v1/bank-accounts/active")
    suspend fun getActiveBankAccount(): Response<ActiveBankAccountResponse>

    @GET("api/v1/wallet/me")
    suspend fun getWalletBalance(): Response<WalletBalanceResponse>

    @Multipart
    @POST("/api/v1/wallet/deposit")
    suspend fun depositFunds(
        @Part("amount") amount: RequestBody,
        @Part("bank_account_id") bankAccountId: RequestBody,
        @Part screenshot: MultipartBody.Part?
    ): Response<ApiResponse<DepositResponse>>

    @Multipart
    @POST("/api/v1/wallet/withdraw")
    suspend fun withdrawFunds(
        @Part("amount") amount: RequestBody,
        @Part("account_name") name: RequestBody,
        @Part("account_number") accountNumber: RequestBody,
        @Part("ifsc_code") ifsc: RequestBody,
        @Part("upi_mobile") upiMobile: RequestBody?
    ): Response<ApiResponse<WithdrawResponse>>

}
