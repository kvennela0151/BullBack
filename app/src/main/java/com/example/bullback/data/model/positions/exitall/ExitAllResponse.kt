package com.example.bullback.data.model.positions.exitall

data class ExitAllResponse(
    val status: Boolean,
    val message: String,
    val data: ExitAllData
)

data class ExitAllData(
    val total: Int,
    val success: Int,
    val failed: Int,
    val results: List<ExitResult>,
    val errors: List<String>
)

data class ExitResult(
    val id: Int,
    val symbol: String,
    val user: String,
    val status: String
)
