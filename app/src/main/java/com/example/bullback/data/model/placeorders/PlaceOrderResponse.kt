package com.example.bullback.data.model.placeorders

import com.example.bullback.data.model.auth.Order

data class PlaceOrderResponse(
    val status: Boolean,
    val message: String,
    val data: Order
)
