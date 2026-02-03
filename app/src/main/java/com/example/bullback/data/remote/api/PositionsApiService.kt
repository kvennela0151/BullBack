package com.example.bullback.data.remote.api

import com.example.bullback.data.model.positions.PositionsResponse
import com.example.bullback.data.model.positions.exitall.ExitAllRequest
import com.example.bullback.data.model.positions.exitall.ExitAllResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PositionsApiService {

    @GET("/api/v1/trade/positions")
    suspend fun getPositions(
        @Query("status") status: String
    ): Response<PositionsResponse>

    @POST("/api/v1/trade/positions/square-off")
    suspend fun exitAllPositions(
        @Body request: ExitAllRequest
    ): Response<ExitAllResponse>

}