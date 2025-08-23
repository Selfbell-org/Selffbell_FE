package com.selfbell.data.repository.impl

import android.provider.ContactsContract
import android.util.Log
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.DeviceTokenUpdateRequest
import com.selfbell.data.api.request.SignupRequest // ✅ import 추가
import com.selfbell.data.api.LoginRequest // ✅ import 추가
import com.selfbell.data.api.MainAddressRequest
import com.selfbell.data.api.ProfileUpdateRequest
import com.selfbell.data.api.RefreshTokenRequest
import com.selfbell.domain.repository.AuthRepository
import javax.inject.Inject
import com.selfbell.data.mapper.toProfile
import com.selfbell.domain.model.Profile

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : AuthRepository {

    // AuthRepository 인터페이스에 정의되지 않은 refreshAccessToken 함수는 AuthRepositoryImpl 클래스에만 존재
    private suspend fun refreshAccessToken(): String? {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                Log.e("AuthRepository", "리프레시 토큰이 없습니다")
                return null
            }

            Log.d("AuthRepository", "액세스 토큰 재발급 요청")
            val request = RefreshTokenRequest(refreshToken)
            val response = authService.refreshToken(request)

            val newAccessToken = response.accessToken?.trim()
            val newRefreshToken = response.refreshToken?.trim()

            if (!newAccessToken.isNullOrBlank()) {
                tokenManager.saveAccessToken(newAccessToken)
                if (!newRefreshToken.isNullOrBlank()) {
                    tokenManager.saveRefreshToken(newRefreshToken)
                }

                Log.d("AuthRepository", "액세스 토큰 재발급 성공")
                return newAccessToken
            } else {
                Log.e("AuthRepository", "유효한 액세스 토큰을 받지 못했습니다")
                return null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "액세스 토큰 재발급 실패: ${e.message}", e)
            return null
        }
    }

    override suspend fun signUp(deviceToken: String, deviceType: String, name: String, phoneNumber: String, password: String) {
        val request = SignupRequest(
            deviceToken = deviceToken,
            deviceType = deviceType,
            name = name,
            phoneNumber = phoneNumber,
            password = password
        )
        try {
            Log.d("AuthRepository", "회원가입 요청: name=$name, phoneNumber=$phoneNumber")
            val response = authService.signup(request)
            Log.d("AuthRepository", "회원가입 응답: message=${response.message}")
        } catch (e: Exception) {
            Log.e("AuthRepository", "회원가입 실패: ${e.message}", e)
            throw e
        }
    }

    override suspend fun login(phoneNumber: String, password: String, deviceToken: String, deviceType: String) {
        val request = LoginRequest(
            phoneNumber = phoneNumber,
            password = password,
            deviceToken = deviceToken,
            deviceType = deviceType
        )
        try {
            Log.d("AuthRepository", "로그인 요청: phoneNumber=$phoneNumber")
            val response = authService.login(request)

            response.accessToken?.let { tokenManager.saveAccessToken(it) }
            response.refreshToken?.let { tokenManager.saveRefreshToken(it) }

            Log.d("AuthRepository", "토큰 저장 완료")
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그인 실패: ${e.message}", e)
            throw e
        }
    }

    override suspend fun registerMainAddress(address: String, lat: Double, lon: Double) {
        if (!tokenManager.hasValidToken()) {
            Log.e("AuthRepository", "메인 주소 등록 실패: 유효한 토큰이 없습니다. 로그인 후 다시 시도해주세요.")
            throw IllegalStateException("로그인이 필요합니다.")
        }
        val request = MainAddressRequest("메인 주소", address, lat, lon)
        try {
            Log.d("AuthRepository", "메인 주소 등록 요청: address=$address")
            val response = authService.registerMainAddress(request)
            if (response.isSuccessful) {
                Log.d("AuthRepository", "메인 주소 등록 성공: ${response.code()}")
            } else {
                Log.e("AuthRepository", "메인 주소 등록 실패: ${response.code()}, ${response.errorBody()?.string()}")
                throw Exception("메인 주소 등록 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "메인 주소 등록 예외: ${e.message}", e)
            throw e
        }
    }
    // ✅ updateDeviceToken 구현
    override suspend fun updateDeviceToken(token: String) {
        try {
            val request = DeviceTokenUpdateRequest(token, "ANDROID") // ✅ 새로운 요청 데이터 클래스
            authService.updateDeviceToken(request)
            Log.d("AuthRepository", "디바이스 토큰 서버 업데이트 성공")
        } catch (e: Exception) {
            Log.e("AuthRepository", "디바이스 토큰 서버 업데이트 실패", e)
            throw e
        }
    }

    override suspend fun logout() {
        try {
            Log.d("AuthRepository", "로그아웃 시작")
            tokenManager.clearTokens()
            Log.d("AuthRepository", "토큰 삭제 완료")
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그아웃 실패: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getUserProfile(): Profile {
        try {
            val dto = authService.getUserProfile()
            return dto.toProfile()
        } catch (e: Exception) {
            Log.e("AuthRepository", "사용자 프로필 가져오기 실패", e)
            throw e
        }
    }

    override suspend fun updateProfile(name: String) {
        val request = ProfileUpdateRequest(name)
        try {
            Log.d("AuthRepository", "프로필 업데이트 요청: name=$name")
            val response = authService.updateProfile(request)
            if (response.isSuccessful) {
                Log.d("AuthRepository", "프로필 업데이트 성공")
            } else {
                Log.e("AuthRepository", "프로필 업데이트 실패: ${response.code()}")
                throw Exception("프로필 업데이트 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "프로필 업데이트 예외: ${e.message}", e)
            throw e
        }
    }
}