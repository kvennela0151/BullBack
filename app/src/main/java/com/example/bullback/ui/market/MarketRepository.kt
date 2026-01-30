package com.example.bullback.ui.market

import com.example.bullback.data.model.auth.CommodityData
import com.example.bullback.data.model.auth.CommodityResponse
import com.example.bullback.data.model.auth.MarginUiModel
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.AuthApi
import com.example.bullback.data.remote.api.MarketApi
import com.example.bullback.utlis.Resource

class MarketRepository(
    private val apiService: MarketApi =
        RetrofitClient.createService(MarketApi::class.java)
) {

    suspend fun getTopCommodities(token: String): Resource<CommodityResponse> {
        return try {
            val response = apiService.getTopCommodities(token)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Empty response from server")
                }
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }

        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Something went wrong")
        }
    }
}