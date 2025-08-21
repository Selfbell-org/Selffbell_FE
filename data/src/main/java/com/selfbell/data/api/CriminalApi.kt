package com.selfbell.data.api

import com.selfbell.data.api.response.CriminalNearbyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CriminalApi {
    
    // 범위 내 범죄자 조회
    @GET("api/v1/criminals/coords/nearby")
    suspend fun getNearbyCriminals(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int
    ): List<CriminalNearbyResponse>
}
