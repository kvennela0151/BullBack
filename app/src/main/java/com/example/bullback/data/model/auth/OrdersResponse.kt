package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class OrdersResponse(
    @SerializedName("success")
    val success: Boolean? = true,

    @SerializedName("data")
    val data: List<Order>? = null,

    @SerializedName("orders")
    val orders: List<Order>? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null
) {
    // Helper to get orders list from either field
    fun getOrdersList(): List<Order> {
        return data ?: orders ?: emptyList()
    }

    // Helper to check if response is successful
    fun isSuccessful(): Boolean {
        return success == true && error == null
    }

    // Helper to get error message
    fun getErrorMessage(): String {
        return error ?: message ?: "Unknown error occurred"
    }
}
