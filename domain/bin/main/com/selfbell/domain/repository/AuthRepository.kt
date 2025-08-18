package com.selfbell.domain.repository

interface AuthRepository {
<<<<<<< HEAD
    suspend fun signUp(deviceToken : String, deviceType: String, name: String, phoneNumber: String, password: String)
=======
    suspend fun signUp(name: String, phoneNumber: String, password: String)
>>>>>>> a9bb50a (chore:Init)

    // 📌 로그인 함수 추가
    suspend fun login(phoneNumber: String, password: String)

<<<<<<< HEAD
    suspend fun registerMainAddress(name: String, address: String, lat: Double, lon: Double)

    // ✅ 로그아웃 함수 추가
    suspend fun logout()
=======
    suspend fun registerMainAddress(token: String, name: String, address: String, lat: Double, lon: Double)

>>>>>>> a9bb50a (chore:Init)
}

data class User(
    val id: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)