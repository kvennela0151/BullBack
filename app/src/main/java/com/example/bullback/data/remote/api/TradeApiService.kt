package com.example.bullback.data.remote.api

import com.example.bullback.data.model.tradelogs.TradePositionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TradeApiService {

    @GET("api/v1/trade/positions")
    suspend fun getTradePositions(
        @Query("status") status: String = "CLOSED",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<TradePositionsResponse>


}
