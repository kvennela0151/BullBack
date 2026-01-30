package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class CommodityResponse(
    val status: Boolean,
    val success: Boolean,
    val data: List<CommodityData>
)

data class CommodityData(

    val token: String? = null,

    @SerializedName("instrument_token")
    val instrumentToken: String,

    @SerializedName("exchange_token")
    val exchangeToken: String,

    @SerializedName("tradingsymbol")
    val tradingSymbol: String,

    val name: String,

    @SerializedName("last_price")
    var lastPrice: Double,

    val expiry: String,
    val strike: Double,

    @SerializedName("tick_size")
    val tickSize: Double,

    @SerializedName("lot_size")
    val lotSize: Int,

    @SerializedName("instrument_type")
    val instrumentType: String,

    val segment: String,
    val exchange: String,
    var change: Double,
    var changePercent: Double
) {
    // Helper properties for display
    val displayName: String
        get() = name

    val displayPrice: Double
        get() = lastPrice

    // Using dummy calculation for demonstration

}