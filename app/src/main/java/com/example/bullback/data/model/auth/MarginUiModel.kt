package com.example.bullback.data.model.auth

data class MarginUiModel(
    val exchange: String,
    val tradingAllowed: Boolean,
    val intraday: Int,
    val holding: Int,
    val strikeRange: Int,
    val maxLot: Int,
    val maxOrderLot: Int,
    val commissionType: String,
    val commissionValue: String
)
