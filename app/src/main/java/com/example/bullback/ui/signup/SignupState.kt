package com.example.bullback.ui.signup

data class SignupState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val referralCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val usernameChecked: Boolean = false,
    val isUsernameAvailable: Boolean = false,
    val usernameCheckMessage: String? = null
)
