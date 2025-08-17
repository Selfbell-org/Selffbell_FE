package com.selfbell.data.repository.impl

import com.selfbell.data.api.HomeService
import com.selfbell.data.api.UserDto
import com.selfbell.domain.HomeRepository
import com.selfbell.domain.User
import javax.inject.Inject

// 토큰 관리를 위한 추상화된 인터페이스 (예시)
interface TokenManager {
    suspend fun getToken(): String?
}

// TokenManager의 구현체
class TokenManagerImpl @Inject constructor() : TokenManager {
    override suspend fun getToken(): String? {
        return "Bearer YOUR_STORED_TOKEN_HERE"
    }
}

class HomeRepositoryImpl @Inject constructor(
    private val homeService: HomeService,
    private val tokenManager: TokenManager
) : HomeRepository {

    override suspend fun getUserProfile(): User {
        val token = tokenManager.getToken()
            ?: throw IllegalStateException("인증 토큰이 존재하지 않습니다.")

        val userDto = homeService.getUserProfile(token)

        // API 응답 DTO를 도메인 모델(User)로 변환
        return userDto.toDomainUser()
    }

    // UserDto를 domain User 모델로 변환하는 확장 함수
    private fun UserDto.toDomainUser(): User {
        return User(
            id = this.id,
            phoneNumber = this.phoneNumber,
            name = this.name, // name 속성 추가
            profileImageUrl = this.profileImageUrl,
            latitude = this.latitude,
            longitude = this.longitude
        )
    }
}