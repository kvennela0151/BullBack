package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistRequest
import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistResponse
import com.example.bullback.data.model.auth.watchlist.addwatchlist.RemoveWatchlistResponse
import com.example.bullback.data.model.auth.watchlist.deletewatchlist.RemoveWatchlistRequest
import com.example.bullback.data.model.auth.watchlist.instrumentsearch.SearchInstrumentResponse
import com.example.bullback.data.model.watchlist.WatchlistResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface WatchlistApiService {
    @GET("/api/v1/trade/watchlist")
    suspend fun getWatchlist(): Response<WatchlistResponse>

    @GET("/api/v1/trade/instruments/search")
    suspend fun searchInstruments(
        @Query("q") query: String,
        @Query("segment") segment: String
    ): Response<SearchInstrumentResponse>

    @POST("/api/v1/trade/watchlist/symbols/remove")
    suspend fun removeSymbols(
        @Body request: RemoveWatchlistRequest
    ): Response<RemoveWatchlistResponse>


    @POST("/api/v1/trade/watchlist/add")
    suspend fun addInstrument(
        @Body request: AddWatchlistRequest
    ): Response<AddWatchlistResponse>

}

