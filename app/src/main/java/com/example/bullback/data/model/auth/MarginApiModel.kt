package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class MarginApiModel(
    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("trading_allowed")
    val tradingAllowed: Boolean,

    @SerializedName("intraday_leverage")
    val intraday: Int,

    @SerializedName("holding_leverage")
    val holding: Int,

    @SerializedName("strike_range")
    val strikeRange: Int,

    @SerializedName("max_lot")
    val maxLot: Int,

    @SerializedName("max_order_lot")
    val maxOrderLot: Int,

    @SerializedName("commission_type")
    val commissionType: String,

    @SerializedName("commission_value")
    val commissionValue: Int
)
