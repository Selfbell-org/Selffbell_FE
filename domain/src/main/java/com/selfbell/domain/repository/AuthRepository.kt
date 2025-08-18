package com.selfbell.domain.repository

interface AuthRepository {
    suspend fun signUp(name: String, phoneNumber: String, password: String)

    // 📌 로그인 함수 추가
    suspend fun login(phoneNumber: String, password: String)
}

data class User(
    val id: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)