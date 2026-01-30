package com.example.bullback.data.repository

import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.PositionsApiService

// PositionsRepository.kt
class PositionsRepository {

    private val api: PositionsApiService = RetrofitClient.createService(PositionsApiService::class.java)

    suspend fun getPositions(status: String): List<PositionsItem> {
        val response = api.getPositions(status)
        if (response.isSuccessful) {
            return response.body()?.data?.positions ?: emptyList()
        } else {
            throw Exception("API failed: ${response.code()} ${response.message()}")
        }
    }
}

