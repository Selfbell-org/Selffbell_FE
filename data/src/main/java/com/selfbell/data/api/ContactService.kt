package com.selfbell.data.api

import com.selfbell.data.api.request.ContactRequestDto
import com.selfbell.data.api.response.ContactListResponseDto
import com.selfbell.data.api.response.ContactResponseDto
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Named

/**
 * 백엔드 연락처 API와의 통신을 위한 Retrofit 서비스 인터페이스
 */
interface ContactService {

    /**
     * 서버에 등록된 연락처 목록을 가져옵니다. (상태별 필터링 가능)
     * @param status 필터링할 관계 상태 (PENDING 또는 ACCEPTED)
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    @GET("/api/v1/contacts")
    suspend fun getContacts(
        @Header("Authorization") token: String,
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ContactListResponseDto

    /**
     * 특정 사용자에게 보호자 요청을 보냅니다.
     * @param toPhoneNumber 요청을 보낼 상대방의 전화번호
     */
    @POST("/api/v1/contacts/requests")
    suspend fun sendContactRequest(
        @Header("Authorization") token: String,
        @Body request: ContactRequestDto
    ): ContactResponseDto

    /**
     * 받은 보호자 요청을 수락합니다.
     * @param contactId 수락할 연락처 요청 ID
     */
    @POST("/api/v1/contacts/{contactId}/accept")
    suspend fun acceptContactRequest(
        @Header("Authorization") token: String,
        @Path("contactId") contactId: Long
    ): ContactResponseDto
}