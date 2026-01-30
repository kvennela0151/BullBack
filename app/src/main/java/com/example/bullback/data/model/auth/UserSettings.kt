package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class UserSettings(

    @SerializedName("margins")
    val margins: List<MarginApiModel>,

    @SerializedName("segment_settings")
    val segmentSettings: Map<String, SegmentSetting>,

    @SerializedName("exchange_segments")
    val exchangeSegments: List<String>
)
