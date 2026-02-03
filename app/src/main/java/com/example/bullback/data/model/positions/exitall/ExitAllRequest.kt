package com.example.bullback.data.model.positions.exitall

data class ExitAllRequest(
    val mode: String = "symbols",
    val symbols: List<String>
)

