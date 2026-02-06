package com.example.bullback.data.model.placeorders

data class PlaceOrderRequest(

    val transaction_type: String,      // "BUY" or "SELL"
    val order_type: String,             // "LIMIT" or "MARKET"
    val quantity: Int,
    val lots: String,
    val price: Double?,
    val stoploss: Double?,
    val target: Double?,
    val exchange: String,
    val instrument_token: String,
    val symbol: String,
    val product_type: String = "MIS"
)
