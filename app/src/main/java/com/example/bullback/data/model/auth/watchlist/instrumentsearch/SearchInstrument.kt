package com.example.bullback.data.model.auth.watchlist.instrumentsearch

import com.google.gson.annotations.SerializedName

// In SearchInstrument.kt
data class SearchInstrument(
    @SerializedName("instrument_token")
    val instrumentToken: Long? = null,

    @SerializedName("exchange_token")
    val exchangeToken: String = "",

    @SerializedName("tradingsymbol")
    val tradingsymbol: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("last_price")
    val lastPrice: Double = 0.0,

    @SerializedName("expiry")
    val expiryDate: String? = null,

    @SerializedName("strike")
    val strike: Double = 0.0,

    @SerializedName("tick_size")
    val tickSize: Double = 0.0,

    @SerializedName("lot_size")
    val lotSize: Int = 0,

    @SerializedName("instrument_type")
    val instrumentType: String = "",

    @SerializedName("segment")
    val segment: String = "",

    @SerializedName("exchange")
    val exchange: String = "",

    // For UI state
    var isAdded: Boolean = false
)
