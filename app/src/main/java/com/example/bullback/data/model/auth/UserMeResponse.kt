package com.example.bullback.data.model.auth

import com.google.gson.annotations.SerializedName

data class UserMeResponse(


    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("is_active")
    val isActive: Boolean,

    @SerializedName("read_only")
    val readOnly: Boolean,

    @SerializedName("is_demo")
    val isDemo: Boolean,

    @SerializedName("settings")
    val settings: UserSettings,

    @SerializedName("created_at")
    val createdAt: String? = null

)
