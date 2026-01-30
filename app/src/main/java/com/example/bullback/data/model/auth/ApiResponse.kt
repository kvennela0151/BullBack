package com.example.bullback.data.model.auth

data class ApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T
)
