package com.selfbell.data.repository.impl

import android.util.Log
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.request.SignupRequest
import com.selfbell.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {
    // 📌 name 파라미터 추가
    override suspend fun signUp(name: String, phoneNumber: String, password: String) {
        val request = SignupRequest(
            name = name,
            phoneNumber = phoneNumber,
            password = password
        )

        try {
            Log.d("AuthRepository", "회원가입 요청: name=$name, phoneNumber=$phoneNumber")
            val response = authService.signup(request)

            if (response.message == "회원가입이 완료되었습니다.") {
                Log.d("AuthRepository", "회원가입 성공: ${response.message}")
            } else {
                Log.d("AuthRepository", "회원가입 응답: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "회원가입 실패: ${e.message}", e)
            throw e
        }
    }
}