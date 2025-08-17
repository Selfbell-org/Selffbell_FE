package com.selfbell.domain.repository


/**
 * 사용자 인증 관련 비즈니스 로직을 위한 추상화된 리포지토리 인터페이스.
 * 이 인터페이스는 어떤 구현체에도 종속되지 않습니다.
 */
interface AuthRepository {

    /**
     * 회원가입을 처리하는 함수.
     *
     * @param phoneNumber 사용자 전화번호
     * @param password    사용자 비밀번호
     */
    suspend fun signUp(phoneNumber: String, password: String)
}

/**
 * 앱의 핵심 사용자 모델.
 *
 * 이 모델은 UI나 데이터 계층의 영향을 받지 않는 순수한 비즈니스 객체입니다.
 */
data class User(
    val id: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)