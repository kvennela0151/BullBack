package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("id")
    val id: Int,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("price")
    val price: String,

    @SerializedName("order_type")
    val orderType: String,

    @SerializedName("transaction_type")
    val transactionType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)
