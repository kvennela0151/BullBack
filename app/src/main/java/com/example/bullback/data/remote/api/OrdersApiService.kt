package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.OrdersResponse
import com.example.bullback.data.model.placeorders.PlaceOrderRequest
import com.example.bullback.data.model.placeorders.PlaceOrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrdersApiService {
    @GET("/api/v1/trade/orders")
    suspend fun getOrders(
        @Query("type") type: String? = null // "OPEN", "EXECUTED", "REJECTED"
    ): OrdersResponse

    @POST("/api/v1/trade/orders")
    suspend fun placeOrder(
        @Body request: PlaceOrderRequest
    ): PlaceOrderResponse
}
