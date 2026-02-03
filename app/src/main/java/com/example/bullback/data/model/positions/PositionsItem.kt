package com.example.bullback.data.model.positions

import com.google.gson.annotations.SerializedName

data class PositionsItem(

    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    val username: String,
    val tradingSymbol: String,

    val symbol: String,

    @SerializedName("instrument_token")
    val instrumentToken: String,
    val exchange: String,
    val segment: String,
    val side: String,

    @SerializedName("product_type")
    val productType: String,

    @SerializedName("net_quantity")
    val netQuantity: String,

    @SerializedName("average_price")
    val averagePrice: String,

    @SerializedName("realized_pnl")
    val realizedPnl: String,

    @SerializedName("buy_price")
    val buyPrice: String,

    @SerializedName("sell_price")
    val sellPrice: Int
)
