package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.OrdersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OrdersApiService {
    @GET("/api/v1/trade/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null
    ): OrdersResponse
}