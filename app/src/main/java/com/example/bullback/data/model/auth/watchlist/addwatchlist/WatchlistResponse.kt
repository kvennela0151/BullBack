package com.example.bullback.data.model.auth.watchlist.addwatchlist

import com.example.bullback.data.model.watchlist.WatchlistSymbol
import com.google.gson.annotations.SerializedName
data class WatchlistData(
    val name: String,
    val symbols: List<WatchlistSymbol>,
    val id: Int,

    @SerializedName("user_id")
    val userId: Int
)

