package com.selfbell.domain.repository

import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactUser

/**
 * 연락처 관련 비즈니스 로직을 위한 추상화된 리포지토리 인터페이스.
 * 이 인터페이스는 어떤 구현체에도 종속되지 않습니다.
 */
interface ContactRepository {

    /**
     * 디바이스 연락처를 불러와서 서버에 등록된 사용자인지 확인합니다.
     * @return ContactUser 목록 (서버 등록 여부 포함)
     */
    //suspend fun loadDeviceContactsWithUserCheck(): List<ContactUser>

    /**
     * 디바이스 연락처만 불러옵니다. (서버 체크 없음)
     */
    //suspend fun loadDeviceContactsOnly(): List<ContactUser>

    /**
     * 특정 전화번호로 가입된 사용자가 존재하는지 확인합니다.
     * @param phoneNumber 확인할 전화번호
     * @return 사용자 존재 여부
     */
    suspend fun checkUserExists(phoneNumber: String): Boolean
    suspend fun getDeviceContacts(): List<ContactUser> // ✅ 로컬 연락처만 가져오는 함수 추가


    /**
     * 서버에 등록된 보호자 연락처 목록을 가져옵니다.
     * @param status 가져올 관계 상태 (PENDING 또는 ACCEPTED)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return ContactRelationship 목록
     */
    suspend fun getContactsFromServer(status: String, page: Int, size: Int): List<ContactRelationship>

    /**
     * 특정 사용자에게 보호자 요청을 보냅니다.
     * @param toPhoneNumber 요청을 보낼 상대방의 전화번호
     */
    suspend fun sendContactRequest(toPhoneNumber: String)

    /**
     * 받은 보호자 요청을 수락합니다.
     * @param contactId 수락할 연락처 요청 ID
     */
    suspend fun acceptContactRequest(contactId: Long)
}