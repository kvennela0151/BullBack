package com.example.bullback.data.model.watchlist

import com.google.gson.annotations.SerializedName

data class WatchlistSymbol(

    val script: String,
    val symbol: String,
    val token: String,

    @SerializedName("instrument_type")
    val instrumentType: String,

    val segment: String,
    val exchange: String,

    @SerializedName("expiry_date")
    val expiryDate: String,

    @SerializedName("lot_size")
    val lotSize: Int,
    val strike: Double,

    var lastPrice: Double,
    var change: Double,

    var changePercent: Double,
    var closePrice: Double
)
