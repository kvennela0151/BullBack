package com.example.bullback.data.model.auth.watchlist.addwatchlist

import com.google.gson.annotations.SerializedName
data class AddWatchlistRequest(
    @SerializedName("script")
    val script: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("instrument_type")
    val instrumentType: String,

    @SerializedName("segment")
    val segment: String,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("expiry_date")
    val expiryDate: String,

    @SerializedName("lot_size")
    val lotSize: Int,

    @SerializedName("strike")
    val strike: Double
)