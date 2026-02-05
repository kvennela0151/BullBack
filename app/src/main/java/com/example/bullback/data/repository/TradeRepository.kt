package com.example.bullback.data.repository

import com.example.bullback.data.model.tradelogs.TradeItem
import com.example.bullback.data.remote.api.TradeApiService

class TradeRepository(private val api: TradeApiService) {

    suspend fun getClosedTrades(limit: Int) =
        api.getTradePositions(limit = limit)

    suspend fun getFilteredTrades(start: String?, end: String?, limit: Int) =
        api.getTradePositions(startDate = start, endDate = end, limit = limit)


}
