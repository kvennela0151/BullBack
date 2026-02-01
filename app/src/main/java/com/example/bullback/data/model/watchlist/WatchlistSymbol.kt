package com.example.bullback.data.model.watchlist

import com.google.gson.annotations.SerializedName

data class WatchlistSymbol(

    val script: String,
    val symbol: String,
    val token: String = "",

    @SerializedName("instrument_token")
    val instrumentToken: String? = null, // nullable

    @SerializedName("instrument_type")
    val instrumentType: String = "",

    val segment: String = "",
    val exchange: String = "",

    @SerializedName("expiry_date")
    val expiryDate: String = "",

    @SerializedName("lot_size")
    val lotSize: Int = 0,
    val strike: Double = 0.0,

    var lastPrice: Double = 0.0,
    var change: Double = 0.0,

    var changePercent: Double = 0.0,
    var closePrice: Double = 0.0,
    @SerializedName("open")
    var openPrice: Double = 0.0,
    @SerializedName("high")
    var highPrice: Double = 0.0,
    @SerializedName("low")
    var lowPrice: Double = 0.0,



    val bid: Double? = null,
    val ask: Double? = null

)

