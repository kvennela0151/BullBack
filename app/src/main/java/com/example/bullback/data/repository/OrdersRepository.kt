package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.Order
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
}

