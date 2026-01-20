package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.Order
import com.example.bullback.data.model.auth.OrderStatus
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.OrdersApiService
import com.example.bullback.utlis.Resource
import retrofit2.HttpException
import java.io.IOException
import kotlin.collections.filter

class OrdersRepository {

    private val apiService: OrdersApiService by lazy {
        RetrofitClient.createService(OrdersApiService::class.java)
    }

    suspend fun getOrders(status: OrderStatus? = null): Resource<List<Order>> {
        return try {
            val statusParam = status?.name?.lowercase()
            val response = apiService.getOrders(statusParam)

            // Check if response is successful using helper method
            if (response.isSuccessful()) {
                Resource.Success(response.getOrdersList())
            } else {
                Resource.Error(response.getErrorMessage())
            }
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message()}")
        } catch (e: IOException) {
            Resource.Error("Network error. Please check your connection.")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }


    // Get filtered orders by status
    fun filterOrdersByStatus(orders: List<Order>, status: OrderStatus): List<Order> {
        return orders.filter { it.orderStatus == status }
    }
}