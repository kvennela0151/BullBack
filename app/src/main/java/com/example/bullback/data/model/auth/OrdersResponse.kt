package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class OrdersResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: OrdersData
)
