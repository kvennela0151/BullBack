package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.ApiResponse
import com.example.bullback.data.model.auth.BankAccount
import com.example.bullback.data.model.wallet.WalletTransaction
import com.example.bullback.data.model.wallet.deposit.DepositResponse
import com.example.bullback.data.model.wallet.depositwithdrawal.ActiveBankAccountResponse
import com.example.bullback.data.model.wallet.walletbalance.WalletBalanceData
import com.example.bullback.data.model.wallet.withdrawal.WithdrawResponse
import com.example.bullback.data.remote.api.WalletApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class WalletRepository(
    private val api: WalletApiService
) {

    suspend fun getTransactions(type: String): List<WalletTransaction> {
        return api.getTransactions(type).data. items
    }

    suspend fun getActiveBankAccount(): Response<ActiveBankAccountResponse> {
        return api.getActiveBankAccount()
    }

    suspend fun getWalletBalance(): WalletBalanceData? {
        val response = api.getWalletBalance()
        if (response.isSuccessful) {
            return response.body()?.data
        }
        return null
    }

    suspend fun depositFunds(
        amount: String,
        bankAccountId: Int,
        screenshot: File?
    ): Response<ApiResponse<DepositResponse>> {

        val amountBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())
        val bankIdBody = bankAccountId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val screenshotPart = screenshot?.let {
            val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("screenshot", it.name, reqFile)
        }

        return api.depositFunds(amountBody, bankIdBody, screenshotPart)
    }

    suspend fun withdrawFunds(
        amount: String,
        name: String,
        accNumber: String,
        ifsc: String,
        upi: String?
    ): Response<ApiResponse<WithdrawResponse>> {

        val amountBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val accBody = accNumber.toRequestBody("text/plain".toMediaTypeOrNull())
        val ifscBody = ifsc.toRequestBody("text/plain".toMediaTypeOrNull())

        val upiBody = upi?.toRequestBody("text/plain".toMediaTypeOrNull())

        return api.withdrawFunds(
            amountBody,
            nameBody,
            accBody,
            ifscBody,
            upiBody
        )
    }



}
