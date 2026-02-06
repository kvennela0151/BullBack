package com.example.bullback.data.repository

import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.positions.exitall.SquareOffRequest
import com.example.bullback.data.model.positions.squareoff.SquareOffResponse
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.PositionsApiService
import com.example.bullback.utlis.Resource

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

    suspend fun squareOffPositions(symbols: List<String>): Resource<SquareOffResponse> {
        return try {
            val request = SquareOffRequest(
                mode = "symbols",
                symbols = symbols
            )
            val response = api.squareOffPositions(request)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to square off positions")
        }
    }

    suspend fun squareOffAllPositions(): Resource<SquareOffResponse> {
        return try {
            val request = SquareOffRequest(
                mode = "all",
                symbols = null
            )
            val response = api.squareOffPositions(request)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to square off all positions")
        }
    }
}

