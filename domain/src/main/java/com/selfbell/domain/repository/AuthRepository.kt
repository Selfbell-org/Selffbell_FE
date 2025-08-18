package com.selfbell.domain.repository

interface AuthRepository {
    // 📌 name 파라미터 추가
    suspend fun signUp(name: String, phoneNumber: String, password: String)
}

data class User(
    val id: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)