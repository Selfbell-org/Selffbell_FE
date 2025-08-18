package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.request.SignupRequest
import com.selfbell.data.api.LoginRequest // LoginRequest import
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.domain.repository.User // User import
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun signUp(name: String, phoneNumber: String, password: String) {
        val request = SignupRequest(
            name = name,
            phoneNumber = phoneNumber,
            password = password
        )
        try {
            Log.d("AuthRepository", "회원가입 요청: name=$name, phoneNumber=$phoneNumber")
            val response = authService.signup(request)
            Log.d("AuthRepository", "회원가입 응답: message=${response.message}")
            // 성공 응답 처리 로직
        } catch (e: Exception) {
            Log.e("AuthRepository", "회원가입 실패: ${e.message}", e)
            throw e
        }
    }

    // 📌 login 함수 추가
    override suspend fun login(phoneNumber: String, password: String) {
        val request = LoginRequest(
            phoneNumber = phoneNumber,
            password = password
        )
        try {
            Log.d("AuthRepository", "로그인 요청: phoneNumber=$phoneNumber")
            val response = authService.login(request)
            Log.d("AuthRepository", "로그인 성공: token=${response.token}")
            // 성공 응답(토큰) 처리 로직
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그인 실패: ${e.message}", e)
            throw e
        }
    }
}