package com.example.bullback.data.model.positions.exitall

import com.google.gson.annotations.SerializedName

data class ExitAllResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ExitAllData?
)

data class ExitAllData(
    @SerializedName("total")
    val total: Int,

    @SerializedName("success")
    val success: Int,

    @SerializedName("failed")
    val failed: Int,

    @SerializedName("results")
    val results: List<ExitResult>,

    @SerializedName("errors")
    val errors: List<ExitError>
)

data class ExitResult(
    @SerializedName("id")
    val id: Int,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("user")
    val user: String,

    @SerializedName("status")
    val status: String
)

data class ExitError(
    @SerializedName("symbol")
    val symbol: String?,

    @SerializedName("error")
    val error: String?
)

/**
 * Request model for square-off API
 * Named SquareOffRequest to match your existing code
 */
data class SquareOffRequest(
    @SerializedName("mode")
    val mode: String,

    @SerializedName("symbols")
    val symbols: List<String>
)