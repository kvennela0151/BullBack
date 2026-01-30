package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class SegmentSetting(
    @SerializedName("trade_allowed")
    val tradeAllowed: Boolean,

    @SerializedName("intraday_leverage")
    val intradayLeverage: Int,

    @SerializedName("holding_leverage")
    val holdingLeverage: Int,

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
