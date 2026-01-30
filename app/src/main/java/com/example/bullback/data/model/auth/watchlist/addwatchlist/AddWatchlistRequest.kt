package com.example.bullback.data.model.auth.watchlist.addwatchlist

import com.google.gson.annotations.SerializedName

data class AddWatchlistRequest(

    val exchange: String,

    @SerializedName("expiry_date")
    val expiryDate: String?,

    @SerializedName("instrument_type")
    val instrumentType: String,

    @SerializedName("lot_size")
    val lotSize: Int,
    val script: String,
    val segment: String,
    val strike: Int,
    val symbol: String,
    val token: String
)
