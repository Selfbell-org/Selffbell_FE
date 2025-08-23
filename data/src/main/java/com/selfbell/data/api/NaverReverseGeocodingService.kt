package com.selfbell.data.api

import com.selfbell.data.api.response.NaverReverseGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverReverseGeocodingService {
    @GET("map-reversegeocode/v2/gc")
    suspend fun reverseGeocode(
        @Query("coords") coords: String, // "경도,위도" 형식
        @Query("output") output: String = "json",
        @Query("orders") orders: String = "roadaddr,addr" // 도로명, 지번 순으로 조회
    ): NaverReverseGeocodeResponse
}