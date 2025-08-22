package com.selfbell.data.api.request

data class SignupRequest(
    val name: String,
    val deviceToken: String, // 📌 이 필드를 추가해야 합니다.
    val phoneNumber: String,
    val password: String,
    val deviceType: String = "ANDROID"
)