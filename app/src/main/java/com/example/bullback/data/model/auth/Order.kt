package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("id")
    val id: Int,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("instrument_token")
    val instrumentToken: String,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("price")
    val price: String,

    @SerializedName("trigger_price")
    val triggerPrice: String?,

    @SerializedName("order_type")
    val orderType: String, // "LIMIT", "MARKET", etc.

    @SerializedName("transaction_type")
    val transactionType: String, // "BUY", "SELL"

    @SerializedName("product_type")
    val productType: String, // "MIS", "CNC", etc.

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("status")
    val status: String, // "OPEN", "EXECUTED", "REJECTED"

    @SerializedName("filled_quantity")
    val filledQuantity: Double,

    @SerializedName("average_price")
    val averagePrice: String,

    @SerializedName("margin_blocked")
    val marginBlocked: String?,

    @SerializedName("reason")
    val reason: String?,

    @SerializedName("stoploss")
    val stoploss: String?,

    @SerializedName("target")
    val target: String?,

    @SerializedName("ip_address")
    val ipAddress: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?
) {
    // Helper property to get OrderStatus enum
    val orderStatus: OrderStatus
        get() = when (status.uppercase()) {
            "OPEN" -> OrderStatus.OPEN
            "EXECUTED", "COMPLETE", "FILLED" -> OrderStatus.EXECUTED
            "REJECTED", "CANCELLED", "CANCELED" -> OrderStatus.REJECTED
            else -> OrderStatus.OPEN
        }

    // Helper property to get OrderSide enum
    val orderSide: OrderSide
        get() = when (transactionType.uppercase()) {
            "BUY" -> OrderSide.BUY
            "SELL" -> OrderSide.SELL
            else -> OrderSide.BUY
        }

    // Helper to get price as double
    val priceDouble: Double
        get() = price.toDoubleOrNull() ?: 0.0

    // Helper to format created date
    val createdAtTimestamp: Long
        get() = try {
            // Parse "2025-12-25T13:34:20" format
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            sdf.parse(createdAt)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
}

enum class OrderStatus {
    OPEN, EXECUTED, REJECTED
}

enum class OrderSide {
    BUY, SELL
}

enum class OrderType {
    LIMIT, MARKET, STOP_LOSS, STOP_LIMIT
}
