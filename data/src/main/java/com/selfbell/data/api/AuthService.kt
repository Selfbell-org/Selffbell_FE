package com.selfbell.data.api


import retrofit2.http.Body
import retrofit2.http.POST

// 서버로 보낼 회원가입 요청 데이터 클래스
data class SignUpRequest(
    val phoneNumber: String,
    val password: String
)

// 서버로 보낼 로그인 요청 데이터 클래스
data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

// 서버로부터 받을 응답 데이터 클래스 (예시)
data class AuthResponse(
    val token: String, // 인증 토큰
    val userId: String,
    val message: String
)

/**
 * 백엔드 API와의 통신을 위한 Retrofit 서비스 인터페이스
 */
interface AuthService {

    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}