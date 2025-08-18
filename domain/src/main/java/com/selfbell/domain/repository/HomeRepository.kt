package com.selfbell.domain.repository

import com.selfbell.domain.User
/**
 * 홈 화면 관련 비즈니스 로직을 위한 리포지토리 인터페이스.
 */
interface HomeRepository {
    suspend fun getUserProfile(): User
}