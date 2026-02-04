package com.example.bullback.data.repository

import com.example.bullback.data.model.tradelogs.TradeItem
import com.example.bullback.data.remote.api.TradeApiService

class TradeRepository(private val api: TradeApiService) {

    suspend fun getClosedTrades(page: Int, limit: Int): List<TradeItem> {
        val res = api.getClosedPositions("CLOSED", page, limit)

        return if (res.isSuccessful) {
            res.body()?.data?.positions ?: emptyList()
        } else emptyList()
    }
}
