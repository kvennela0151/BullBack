package com.example.bullback.data.model.watchlist

import com.example.bullback.data.model.auth.watchlist.addwatchlist.WatchlistData

data class WatchlistResponse(

    val status: Boolean,
    val message: String,
    val data: List<WatchlistData>   // LIST
)
