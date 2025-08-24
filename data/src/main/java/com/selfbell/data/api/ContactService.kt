package com.selfbell.data.api

import com.selfbell.data.api.request.ContactRequestDto
import com.selfbell.data.api.request.ContactRequestRequest
import com.selfbell.data.api.response.ContactListResponseDto
import com.selfbell.data.api.response.ContactResponseDto
import com.selfbell.data.api.response.FCMTokenResponse
import com.selfbell.data.api.response.UserExistsResponse
import com.selfbell.data.api.response.UserInfoResponse
import com.selfbell.data.api.response.ContactRequestResponse
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Named

/**
 * 백엔드 연락처 API와의 통신을 위한 Retrofit 서비스 인터페이스
 */
interface ContactService {

    /**
     * 특정 전화번호로 가입된 사용자가 존재하는지 확인합니다.
     * @param phoneNumber 확인할 전화번호
     */
    @GET("/api/v1/users")
    suspend fun checkUserExists(
        @Query("phoneNumber") phoneNumber: String
    ): UserExistsResponse

    /**
     * 특정 전화번호로 가입된 사용자 정보를 가져옵니다.
     * @param phoneNumber 확인할 전화번호
     */
    @GET("/api/v1/users/info")
    suspend fun getUserInfo(
        @Query("phoneNumber") phoneNumber: String
    ): UserInfoResponse

    /**
     * 서버에 등록된 연락처 목록을 가져옵니다. (상태별 필터링 가능)
     * @param status 필터링할 관계 상태 (PENDING 또는 ACCEPTED)
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    @GET("/api/v1/contacts")
    suspend fun getContacts(
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ContactListResponseDto

    /**
     * 특정 사용자에게 보호자 요청을 보냅니다.
     * @param request 보호자 요청 데이터
     */
    @POST("/api/v1/contacts/requests")
    suspend fun sendContactRequest(
        @Body request: ContactRequestRequest
    ): Response<ContactRequestResponse>

    /**
     * 받은 보호자 요청을 수락합니다.
     * @param contactId 수락할 연락처 요청 ID
     */
    @POST("/api/v1/contacts/{contactId}/accept")
    suspend fun acceptContactRequest(
        @Path("contactId") contactId: Long
    ): ContactResponseDto

    /**
     * 특정 사용자의 FCM 토큰을 가져옵니다.
     * @param userId FCM 토큰을 가져올 사용자 ID
     */
    @GET("/api/v1/users/{userId}/fcm-token")
    suspend fun getUserFCMToken(
        @Path("userId") userId: String
    ): FCMTokenResponse
}