package com.example.bullback.data.remote.api

import com.example.bullback.data.model.tradelogs.TradePositionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TradeApiService {

    @GET("api/v1/trade/positions")
    suspend fun getClosedPositions(
        @Query("status") status: String = "CLOSED",
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<TradePositionsResponse>

}
