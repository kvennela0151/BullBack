package com.example.bullback.data.remote.api

import com.example.bullback.data.model.positions.PositionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PositionsApiService {

    @GET("/api/v1/trade/positions")
    suspend fun getPositions(
        @Query("status") status: String
    ): Response<PositionsResponse>

}