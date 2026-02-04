package com.example.bullback.data.model.tradelogs

data class TradePositionsResponse(
    val status: Boolean,
    val message: String,
    val data: TradePositionData
)

data class TradePositionData(
    val positions: List<TradeItem>
)

data class TradeItem(
    val id: Int,
    val symbol: String,
    val exchange: String,
    val product_type: String,
    val segment: String,
    val buy_quantity: Int,
    val sell_quantity: Int,
    val buy_price: String,
    val sell_price: String,
    val realized_pnl: String,
    val brokerage: String,
    val side: String,
    val created_at: String,
    val updated_at: String,
    val script: String,
    val lots: String
)
