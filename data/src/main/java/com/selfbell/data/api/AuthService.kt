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
import retrofit2.http.PUT // ✅ PUT 메서드 import
import retrofit2.http.PATCH // ✅ PATCH 메서드 import

// 서버로 보낼 회원가입 요청 데이터 클래스 (AuthViewModel의 signUp 함수와 일치)
data class SignupRequest(
    val deviceToken : String,
    val deviceType: String,
    val name: String,
    val phoneNumber: String,
    val password: String
)

// ✅ 서버로 보낼 로그인 요청 데이터 클래스 수정
data class LoginRequest(
    val phoneNumber: String,
    val password: String,
    val deviceToken: String, // ✅ 추가
    val deviceType: String   // ✅ 추가
)

// ✅ 프로필 업데이트 요청 데이터 클래스 추가
data class ProfileUpdateRequest(
    val name: String // ✅ 이름만 변경하는 요청
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

// ✅ 메인 주소 등록 요청 데이터 클래스 수정
data class MainAddressRequest(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
)

// ✅ 디바이스 토큰 업데이트 요청 데이터 클래스
data class DeviceTokenUpdateRequest(
    val deviceToken: String,
    val deviceType: String
)
/**
 * 백엔드 API와의 통신을 위한 Retrofit 서비스 인터페이스
 */
interface AuthService {

    @POST("/api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    // ✅ login 함수 수정
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // 프로필 조회 (인터셉터가 Authorization 헤더 자동 추가)
    @GET("/api/v1/users/profile")
    suspend fun getUserProfile(): ProfileResponseDto

    // ✅ 프로필 업데이트 API 추가 (PUT 또는 PATCH 사용)
    @PATCH("/api/v1/users/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<Unit>

    /**
     * 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급받습니다.
     * @param request 리프레시 토큰 요청 데이터
     * @return 새로운 액세스 토큰과 리프레시 토큰
     */

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    // ✅ registerMainAddress 함수 수정
    @POST("/api/v1/addresses")
    suspend fun registerMainAddress(
        @Body request: MainAddressRequest
    ): Response<Unit> // 응답 바디가 없을 경우 Response<Unit> 사용



    // ✅ 디바이스 토큰 업데이트 API 엔드포인트 추가
    @PATCH("/api/v1/users/device-token")
    suspend fun updateDeviceToken(@Body request: DeviceTokenUpdateRequest): Response<Unit>
}