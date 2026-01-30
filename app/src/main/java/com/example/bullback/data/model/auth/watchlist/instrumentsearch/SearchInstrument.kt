package com.example.bullback.data.model.auth.watchlist.instrumentsearch

import com.google.gson.annotations.SerializedName

data class SearchInstrument(

    @SerializedName("instrument_token")
    val instrumentToken: Long,

    @SerializedName("tradingsymbol")
    val symbol: String,
    @SerializedName("expiry")
    val expiryDate: String?,

    val segment: String,
    val exchange: String,

    @SerializedName("lot_size")
    val lotSize: Int,


    var isAdded: Boolean = false,

    val instrumentType: String,
    val script: String,
    val strike: Int

)
