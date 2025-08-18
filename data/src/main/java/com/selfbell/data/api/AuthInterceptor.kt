package com.selfbell.data.api

import com.selfbell.data.repository.impl.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        
        // 토큰을 동기적으로 가져오기 위해 runBlocking 사용
        val token = runBlocking { tokenManager.getAccessToken() }
        val cleanedToken = token?.trim()
        
        // 토큰이 존재하고 공백이 아닐 때만 Authorization 헤더 추가
        if (!cleanedToken.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $cleanedToken")
        }
        
        return chain.proceed(request.build())
    }
} 