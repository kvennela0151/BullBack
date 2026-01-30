package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)
