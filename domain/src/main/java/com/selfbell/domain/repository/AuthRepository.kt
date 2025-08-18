package com.selfbell.domain.repository

interface AuthRepository {
    suspend fun signUp(deviceToken : String, deviceType: String, name: String, phoneNumber: String, password: String)

    // 📌 로그인 함수 추가
    suspend fun login(phoneNumber: String, password: String)

    suspend fun registerMainAddress(token: String, name: String, address: String, lat: Double, lon: Double)

}

data class User(
    val id: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)