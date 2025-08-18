package com.selfbell.data.api

import com.selfbell.data.api.response.EmergencyBellNearbyResponse
import com.selfbell.data.api.response.EmergencyBellDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EmergencyBellApi {

    // 범위 내 안심벨 조회
    @GET("api/v1/emergency-bells/nearby")
    suspend fun getNearbyEmergencyBells(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int
    ): EmergencyBellNearbyResponse

    // 안심벨 상세 조회
    @GET("api/v1/emergency-bells/{objt_ID}")
    suspend fun getEmergencyBellDetail(
        @Path("objt_ID") objtId: Int
    ): EmergencyBellDetailResponse
}