package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.ApiResponse
import com.example.bullback.data.model.auth.CheckUsernameResponse
import com.example.bullback.data.model.auth.LoginResponse
import com.example.bullback.data.model.auth.SignupResponse
import com.example.bullback.data.model.auth.UserMeResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi {
    // Login API using form-data
    @Multipart
    @POST("api/v1/auth/login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>

    // Signup API using form-data - ADD ALL YOUR FIELDS
    @Multipart
    @POST("api/v1/auth/register")
    suspend fun signup(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("email") email: RequestBody,
        @Part("fullName") fullName: RequestBody,  // Changed from firstName
        @Part("phone") phone: RequestBody,        // Added phone
        @Part("referralCode") referralCode: RequestBody? = null
    ): Response<SignupResponse>

    // Demo login
    @GET("/api/v1/auth/instant-demo")
    suspend fun demoLogin(): Response<LoginResponse>

    // Check username
    @GET("/api/v1/users/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<CheckUsernameResponse>

    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<ApiResponse<UserMeResponse>>


}
