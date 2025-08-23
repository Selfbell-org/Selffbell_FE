package com.selfbell.data.api

import com.selfbell.data.api.response.*
import retrofit2.Response
import retrofit2.http.*

interface SafeWalksApi {
    @POST("api/v1/safe-walks")
    suspend fun createSafeWalkSession(
        @Body request: SafeWalkCreateRequest
    ): SafeWalkCreateResponse

    @POST("api/v1/safe-walks/{sessionId}/track")
    suspend fun uploadLocationTrack(
        @Path("sessionId") sessionId: Long,
        @Body request: TrackRequest
    ): TrackResponse

    @PUT("api/v1/safe-walks/{sessionId}/end")
    suspend fun endSafeWalkSession(
        @Path("sessionId") sessionId: Long,
        @Body request: EndRequest
    ): EndResponse

    @GET("api/v1/safe-walks/{sessionId}")
    suspend fun getSafeWalkDetail(
        @Path("sessionId") sessionId: Long
    ): SafeWalkDetailResponse

    @GET("api/v1/safe-walks/ward/current")
    suspend fun getCurrentSafeWalk(): CurrentSafeWalkResponse?

    @GET("api/v1/safe-walks/{sessionId}/tracks")
    suspend fun getTracks(
        @Path("sessionId") sessionId: Long,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int?,
        @Query("order") order: String?
    ): TracksResponse

    @GET("api/v1/safe-walks/history")
    suspend fun getHistory(
        @Query("target") target: String // "me" 또는 "ward"
    ): HistoryListResponse
}

