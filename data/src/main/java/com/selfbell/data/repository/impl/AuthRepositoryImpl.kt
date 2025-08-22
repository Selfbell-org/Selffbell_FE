package com.selfbell.data.repository.impl

import android.provider.ContactsContract
import android.util.Log
import com.selfbell.data.api.AuthService
import com.selfbell.data.api.request.SignupRequest
import com.selfbell.data.api.LoginRequest // LoginRequest import
import com.selfbell.data.api.LoginResponse
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.domain.repository.User // User import
import javax.inject.Inject
import com.selfbell.data.api.MainAddressRequest // 📌 import
import com.selfbell.data.mapper.toProfile
import com.selfbell.domain.model.Profile

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun signUp(deviceToken : String,deviceType : String, name: String, phoneNumber: String, password: String) {
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
//        val response = LoginResponse(
//            accessToken = accessToken,
//            refreshToken = refreshToken
//        )
        try {
            Log.d("AuthRepository", "로그인 요청: phoneNumber=$phoneNumber")
            val response = authService.login(request)
            Log.d("AuthRepository", "로그인 성공: token=${response.accessToken}")
            
            // ✅ 토큰을 안전한 저장소에 저장
            response.accessToken?.let { tokenManager.saveAccessToken(it) }
            response.refreshToken?.let { tokenManager.saveRefreshToken(it) }
            
            Log.d("AuthRepository", "토큰 저장 완료")
        } catch (e: Exception) {
            Log.e("AuthRepository", "로그인 실패: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun registerMainAddress(name: String, address: String, lat: Double, lon: Double) {
        // ✅ 사전 체크: 토큰이 없으면 바로 실패 처리
        if (!tokenManager.hasValidToken()) {
            Log.e("AuthRepository", "메인 주소 등록 실패: 유효한 토큰이 없습니다. 로그인 후 다시 시도해주세요.")
            throw IllegalStateException("로그인이 필요합니다.")
        }

        val request = MainAddressRequest(name, address, lat, lon)
        try {
            Log.d("AuthRepository", "메인 주소 등록 요청: name=$name, address=$address")
            // ✅ AuthInterceptor가 자동으로 토큰을 추가하므로 토큰 파라미터 불필요
            val response = authService.registerMainAddress(request)
            if (response.isSuccessful) {
                Log.d("AuthRepository", "메인 주소 등록 성공: ${response.code()}")
            } else {
                val errorBody = response.errorBody()?.string()
            Log.e("AuthRepository", "메인 주소 등록 실패: ${response.code()}, $errorBody")
                throw Exception("메인 주소 등록 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "메인 주소 등록 예외: ${e.message}", e)
            throw e
        }
    }

    // ✅ 로그아웃 구현
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

    suspend fun refreshAccessToken(): String? {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                Log.e("AuthRepository", "리프레시 토큰이 없습니다")
                return null
            }

            Log.d("AuthRepository", "액세스 토큰 재발급 요청")
            val request = com.selfbell.data.api.RefreshTokenRequest(refreshToken)
            val response = authService.refreshToken(request)

            val newAccessToken = response.accessToken?.trim()
            val newRefreshToken = response.refreshToken?.trim()

            if (!newAccessToken.isNullOrBlank()) {
                // 새 토큰들 저장
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

    // 프로필 조회 추가
    suspend fun getUserProfile(): Profile {
        try {
            val dto = authService.getUserProfile()
            return dto.toProfile()
        } catch (e: Exception) {
            Log.e("AuthRepository", "사용자 프로필 가져오기 실패", e)
            throw e
        }
    }
}