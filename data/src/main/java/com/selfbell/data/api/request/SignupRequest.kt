package com.selfbell.data.api.request

data class SignupRequest(
    val name: String,
    val phoneNumber: String,
    val password: String,
    val deviceType: String = "ANDROID"
)