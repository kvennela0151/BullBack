package com.example.bullback.data.model.auth

data class SignupRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val referralCode: String? = null
)
