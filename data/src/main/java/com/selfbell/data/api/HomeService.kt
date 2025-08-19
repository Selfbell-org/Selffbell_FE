package com.selfbell.data.api

// data/api/HomeService.kt

import retrofit2.http.GET
import retrofit2.http.Header
import com.google.gson.annotations.SerializedName
/**
 * 서버로부터 받을 사용자 프로필 응답 데이터.
 * API 응답에 맞춰 필드를 정의합니다.
 */


data class UserDto(
    val id: String,
    val phoneNumber: String,
    val name: String?,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    val latitude: Double,
    val longitude: Double
)

/**
 * 홈 화면 관련 API 호출을 위한 서비스 인터페이스.
 * 인증이 필요한 요청을 처리합니다.
 */
interface HomeService {
    /**
     * 사용자 프로필을 가져오는 API 호출.
     * @param token 인증 토큰
     * @return 사용자 프로필 데이터
     */
    @GET("api/user/profile")
    suspend fun getUserProfile(
        // @Header를 사용하여 요청 헤더에 인증 토큰을 추가합니다.
        @Header("Authorization") token: String
    ): UserDto

}