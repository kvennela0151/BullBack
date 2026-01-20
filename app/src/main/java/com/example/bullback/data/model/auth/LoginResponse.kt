package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("message")
    val message: String? = null
)
