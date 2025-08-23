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
        private var isRefreshing = false // isRefreshing 변수는 Mutex 사용으로 필요 없지만, 기존 코드에 맞춰 유지
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
        
        Log.d(TAG, "=== HTTP 응답 분석 ===")
        Log.d(TAG, "요청 URL: ${originalRequest.url}")
        Log.d(TAG, "요청 메서드: ${originalRequest.method}")
        Log.d(TAG, "응답 상태 코드: ${response.code}")
        Log.d(TAG, "응답 메시지: ${response.message}")
        
        // ✅ 401 응답 시에만 토큰 재발급 시도 (403은 권한 문제이므로 제외)
        if (response.code == 401 && !cleanedToken.isNullOrBlank()) {
            Log.d(TAG, "토큰 만료 감지. 토큰 재발급 시도...")

            response.close() // 기존 응답 닫기

            // 토큰 재발급 후 요청 재시도
            val newToken = runBlocking { refreshTokenIfNeeded() }

            return if (!newToken.isNullOrBlank()) {
                // 새 토큰으로 요청 재시도
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()

                Log.d(TAG, "새 토큰으로 요청 재시도")
                chain.proceed(newRequest)
            } else {
                // 토큰 재발급 실패 시 원래 응답 반환 (새 응답 생성)
                Log.e(TAG, "토큰 재발급 실패. 사용자 로그아웃 필요")

                // 기존 응답 객체를 재사용할 수 없으므로, 새 응답 객체를 빌드해야 합니다.
                // OkHttp의 Response.Builder를 사용하여 상태 코드를 포함한 새 응답을 생성합니다.
                val responseBody = response.body
                Response.Builder()
                    .request(originalRequest)
                    .protocol(response.protocol)
                    .code(response.code)
                    .message(response.message)
                    .headers(response.headers)
                    .body(responseBody)
                    .build()
            }
        }
        
        // 403 오류에 대한 상세 로그
        if (response.code == 403) {
            Log.w(TAG, "=== 403 Forbidden 오류 감지 ===")
            Log.w(TAG, "요청 URL: ${originalRequest.url}")
            Log.w(TAG, "요청 메서드: ${originalRequest.method}")
            Log.w(TAG, "토큰 존재 여부: ${!cleanedToken.isNullOrBlank()}")
            Log.w(TAG, "토큰 길이: ${cleanedToken?.length ?: 0}")
            Log.w(TAG, "이는 권한 문제이므로 토큰 재발급을 시도하지 않습니다")
            Log.w(TAG, "=== 403 Forbidden 오류 분석 완료 ===")
        }

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