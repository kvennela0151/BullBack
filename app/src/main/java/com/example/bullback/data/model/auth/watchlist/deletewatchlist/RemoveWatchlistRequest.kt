package com.example.bullback.data.model.auth.watchlist.deletewatchlist

data class RemoveWatchlistRequest(
    val id: Int,
    val symbols: List<String>
)
