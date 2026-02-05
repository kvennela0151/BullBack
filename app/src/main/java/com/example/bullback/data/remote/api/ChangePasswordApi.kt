package com.example.bullback.data.remote.api

import com.example.bullback.data.model.changepassword.ChangePasswordRequest
import com.example.bullback.data.model.changepassword.ChangePasswordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChangePasswordApi {

    @POST("/api/v1/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>
}