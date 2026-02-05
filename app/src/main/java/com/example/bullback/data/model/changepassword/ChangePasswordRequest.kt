package com.example.bullback.data.model.changepassword

import com.google.gson.annotations.SerializedName

// Request Model
data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("confirm_password")
    val confirmPassword: String
)

// Response Models
data class ChangePasswordResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: PasswordChangeData?
)

data class PasswordChangeData(
    @SerializedName("status")
    val status: Boolean
)