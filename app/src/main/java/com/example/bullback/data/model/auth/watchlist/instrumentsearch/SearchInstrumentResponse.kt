package com.example.bullback.data.model.auth.watchlist.instrumentsearch

data class SearchInstrumentResponse(

    val status: Boolean,
    val message: String,
    val data: List<SearchInstrument>
)
