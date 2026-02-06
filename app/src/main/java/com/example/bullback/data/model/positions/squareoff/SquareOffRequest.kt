package com.example.bullback.data.model.positions.squareoff

data class SquareOffRequest(

    val mode: String,  // "symbols" or "all"
    val symbols: List<String>? = null  // Only needed when mode = "symbols"
)
