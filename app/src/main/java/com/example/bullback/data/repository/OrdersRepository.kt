package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.Order
import com.example.bullback.data.model.auth.OrdersResponse
import com.example.bullback.data.model.placeorders.PlaceOrderRequest
import com.example.bullback.data.model.placeorders.PlaceOrderResponse
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.OrdersApiService
import com.example.bullback.utlis.Resource


class OrdersRepository(
    private val apiService: OrdersApiService =
        RetrofitClient.createService(OrdersApiService::class.java)
) {

    suspend fun getOrders(type: String): Resource<List<Order>> {
        return try {
            val response = apiService.getOrders(type)
            Resource.Success(response.data.orders)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Something went wrong")
        }
    }

    suspend fun placeOrder(request: PlaceOrderRequest): Resource<PlaceOrderResponse> {
        return try {
            val response = apiService.placeOrder(request)
            // Simply return success with the response
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to place order")
        }
    }

}

