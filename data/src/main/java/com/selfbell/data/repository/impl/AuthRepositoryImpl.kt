package com.selfbell.data.repository.impl


import com.selfbell.data.api.AuthService // AuthService import
import com.selfbell.data.api.SignUpRequest // SignUpRequest import
import com.selfbell.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun signUp(phoneNumber: String, password: String) {
        // 백엔드 API 호출을 수행하는 부분입니다.
        authService.signUp(
            request = SignUpRequest(
                phoneNumber = phoneNumber,
                password = password
            )
        )
    }
}