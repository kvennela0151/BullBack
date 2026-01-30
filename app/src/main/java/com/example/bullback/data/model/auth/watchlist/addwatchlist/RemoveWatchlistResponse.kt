package com.example.bullback.data.model.auth.watchlist.addwatchlist

data class RemoveWatchlistResponse(
    val status: Boolean,
    val message: String,
    val data: List<WatchlistData>
)
