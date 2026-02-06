package com.example.bullback.data.model.positions.squareoff

data class SquareOffData(

    val total: Int,
    val success: Int,
    val failed: Int,
    val results: List<SquareOffResult>,
    val errors: List<String>
)
