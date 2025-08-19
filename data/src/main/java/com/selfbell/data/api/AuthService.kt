package com.selfbell.data.api


import com.selfbell.data.api.request.SignupRequest
import com.selfbell.data.api.response.SignupResponse
import com.selfbell.data.di.DataModule
import com.google.gson.annotations.SerializedName
import com.selfbell.data.api.response.ProfileResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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

// 리프레시 토큰 요청 데이터 클래스
data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LoginResponse(
    @SerializedName(value = "accessToken", alternate = ["access_token", "token"]) val accessToken: String?,
    @SerializedName(value = "refreshToken", alternate = ["refresh_token"]) val refreshToken: String?
)

// 토큰 재발급 응답 데이터 클래스
data class RefreshTokenResponse(
    @SerializedName(value = "accessToken", alternate = ["access_token", "token"]) val accessToken: String?,
    @SerializedName(value = "refreshToken", alternate = ["refresh_token"]) val refreshToken: String?
)

// 서버로부터 받을 응답 데이터 클래스 (예시)
data class AuthResponse(
    val token: String, // 인증 토큰
    val userId: String,
    val message: String
)

data class MainAddressRequest(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
)

/**
 * 백엔드 API와의 통신을 위한 Retrofit 서비스 인터페이스
 */
interface AuthService {

    @POST("/api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/v1/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): ProfileResponseDto
    /**
     * 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급받습니다.
     * @param request 리프레시 토큰 요청 데이터
     * @return 새로운 액세스 토큰과 리프레시 토큰
     */

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @POST("/api/v1/addresses")
    suspend fun registerMainAddress(
        @Body request: MainAddressRequest
    ): Response<Unit> // 응답 바디가 없을 경우 Response<Unit> 사용
}