package com.example.bullback.data.model.auth

data class DemoLoginResponse(
    val token: String,
    val user: User,
    val demoAccount: Boolean,
    val message: String?

)
