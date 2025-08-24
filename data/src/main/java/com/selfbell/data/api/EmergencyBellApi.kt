package com.selfbell.data.api

import com.selfbell.data.api.response.EmergencyBellNearbyResponse
import com.selfbell.data.api.response.EmergencyBellDetailResponse
import com.selfbell.data.api.request.SosMessageRequest
import com.selfbell.data.api.response.SosMessageResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body

interface EmergencyBellApi {

    // 범위 내 안심벨 조회
    @GET("api/v1/emergency-bells/nearby")
    suspend fun getNearbyEmergencyBells(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int
    ): EmergencyBellNearbyResponse

    // 안심벨 상세 조회
    @GET("api/v1/emergency-bells/{objt_id}")
    suspend fun getEmergencyBellDetail(
        @Path("objt_id") id: Int
    ): EmergencyBellDetailResponse

    // SOS 메시지 전송
    @POST("api/v1/sos/messages")
    suspend fun sendSosMessage(@Body request: SosMessageRequest): SosMessageResponse
}