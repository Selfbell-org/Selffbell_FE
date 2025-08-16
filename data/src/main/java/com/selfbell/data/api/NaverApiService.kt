package com.selfbell.data.api

import com.selfbell.data.api.response.NaverGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverApiService {
    @GET("map-geocode/v2/geocode")
    suspend fun getGeocode(
        @Query("query") query: String,
        @Query("count") count: Int = 3 // 반환할 검색 결과 개수
    ): NaverGeocodeResponse
}