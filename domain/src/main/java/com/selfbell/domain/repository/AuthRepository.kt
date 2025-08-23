package com.selfbell.domain.repository

import com.selfbell.domain.model.Profile

interface AuthRepository {
    suspend fun signUp(deviceToken: String, deviceType: String, name: String, phoneNumber: String, password: String)

    // ✅ login 함수에 FCM 토큰 관련 파라미터 추가
    suspend fun login(phoneNumber: String, password: String, deviceToken: String, deviceType: String)

    // ✅ registerMainAddress 함수에서 name 파라미터 제거
    suspend fun registerMainAddress(address: String, lat: Double, lon: Double)

    // ✅ getUserProfile 함수 추가
    suspend fun getUserProfile(): Profile

    // ✅ 프로필 업데이트 함수 추가
    suspend fun updateProfile(name: String)

    suspend fun updateDeviceToken(token: String)

    // ✅ 로그아웃 함수 추가
    suspend fun logout()
}