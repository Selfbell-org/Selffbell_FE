package com.selfbell.domain.repository

import com.selfbell.domain.model.ContactRelationship

/**
 * 연락처 관련 비즈니스 로직을 위한 추상화된 리포지토리 인터페이스.
 * 이 인터페이스는 어떤 구현체에도 종속되지 않습니다.
 */
interface ContactRepository {

    /**
     * 서버에 등록된 보호자 연락처 목록을 가져옵니다.
     * @param token 사용자 인증 토큰
     * @param status 가져올 관계 상태 (PENDING 또는 ACCEPTED)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return ContactRelationship 목록
     */
    suspend fun getContactsFromServer(token: String, status: String, page: Int, size: Int): List<ContactRelationship>

    /**
     * 특정 사용자에게 보호자 요청을 보냅니다.
     * @param token 사용자 인증 토큰
     * @param toPhoneNumber 요청을 보낼 상대방의 전화번호
     */
    suspend fun sendContactRequest(token: String, toPhoneNumber: String)

    /**
     * 받은 보호자 요청을 수락합니다.
     * @param token 사용자 인증 토큰
     * @param contactId 수락할 연락처 요청 ID
     */
    suspend fun acceptContactRequest(token: String, contactId: Long)
}