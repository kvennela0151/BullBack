package com.example.bullback.data.model.auth

import androidx.room.Index
import com.google.gson.annotations.SerializedName


data class OrdersData(

    @SerializedName("orders")
    val orders: List<Order>
)




