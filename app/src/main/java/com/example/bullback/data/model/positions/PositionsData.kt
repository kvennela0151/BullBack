package com.example.bullback.data.model.positions

import com.google.gson.annotations.SerializedName

data class PositionsData(

    val positions: List<PositionsItem>,

    @SerializedName("total_count")
    val totalCount: Int,

    @SerializedName("current_count")
    val currentCount: Int,

    @SerializedName("total_page")
    val totalPage: Int,

    val page: Int,
    val limit: Int
)
