package com.example.auth.ui

// auth/AuthRepository.kt

import com.selfbell.auth.network.AuthService // API 서비스 인터페이스
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun signUp(phoneNumber: String, password: String): Result<Unit> {
        return try {
            authService.registerUser(phoneNumber, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}