package com.selfbell.data.api

import android.util.Log
import com.selfbell.data.repository.impl.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private val refreshMutex = Mutex()
        private var isRefreshing = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 토큰을 동기적으로 가져오기 위해 runBlocking 사용
        val token = runBlocking { tokenManager.getAccessToken() }
        val cleanedToken = token?.trim()

        // 요청에 Authorization 헤더 추가
        val requestWithAuth = if (!cleanedToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $cleanedToken")
                .build()
        } else {
            originalRequest
        }

        // 요청 실행
        val response = chain.proceed(requestWithAuth)

        // 401 또는 403 응답 시 토큰 재발급 시도
//        if ((response.code == 401 || response.code == 403) && !cleanedToken.isNullOrBlank()) {
//            Log.d(TAG, "토큰 만료 감지. 토큰 재발급 시도...")
//
//            response.close() // 기존 응답 닫기
//
//            // 토큰 재발급 후 요청 재시도
//            val newToken = runBlocking { refreshTokenIfNeeded() }
//
//            return if (!newToken.isNullOrBlank()) {
//                // 새 토큰으로 요청 재시도
//                val newRequest = originalRequest.newBuilder()
//                    .addHeader("Authorization", "Bearer $newToken")
//                    .build()
//
//                Log.d(TAG, "새 토큰으로 요청 재시도")
//                chain.proceed(newRequest)
//            } else {
//                // 토큰 재발급 실패 시 원래 응답 반환
//                Log.e(TAG, "토큰 재발급 실패. 사용자 로그아웃 필요")
//                response
//            }
//        }

        return response
    }

    /**
     * 토큰이 필요한 경우에만 재발급을 수행합니다.
     * 동시에 여러 요청에서 토큰 재발급이 일어나지 않도록 Mutex를 사용합니다.
     */
    private suspend fun refreshTokenIfNeeded(): String? {
        return refreshMutex.withLock {
            try {
                // 다른 스레드에서 이미 토큰이 갱신되었는지 확인
                val currentToken = tokenManager.getAccessToken()
                
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken.isNullOrBlank()) {
                    Log.e(TAG, "리프레시 토큰이 없습니다. 로그아웃 필요")
                    tokenManager.clearTokens()
                    return null
                }

                Log.d(TAG, "리프레시 토큰으로 새 액세스 토큰 요청")
                val refreshRequest = RefreshTokenRequest(refreshToken)
                val refreshResponse = authService.refreshToken(refreshRequest)

                val newAccessToken = refreshResponse.accessToken?.trim()
                val newRefreshToken = refreshResponse.refreshToken?.trim()

                if (!newAccessToken.isNullOrBlank()) {
                    // 새 토큰들 저장
                    tokenManager.saveAccessToken(newAccessToken)
                    if (!newRefreshToken.isNullOrBlank()) {
                        tokenManager.saveRefreshToken(newRefreshToken)
                    }
                    
                    Log.d(TAG, "토큰 재발급 성공")
                    return newAccessToken
                } else {
                    Log.e(TAG, "토큰 재발급 응답에 유효한 액세스 토큰이 없습니다")
                    tokenManager.clearTokens()
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 재발급 중 오류 발생", e)
                tokenManager.clearTokens()
                return null
            }
        }
    }
} 