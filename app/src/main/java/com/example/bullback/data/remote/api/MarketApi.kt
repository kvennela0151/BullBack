package com.example.bullback.data.remote.api

import com.example.bullback.data.model.auth.CommodityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MarketApi {

    @GET("api/v1/trade/instruments/top-commodities")
    suspend fun getTopCommodities(
        @Header("Authorization") authorization: String
    ): Response<CommodityResponse>
}