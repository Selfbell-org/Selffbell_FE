package com.selfbell.domain.repository

import com.selfbell.domain.model.*
import java.time.LocalDateTime

interface SafeWalkRepository {
    // ✅ SafeWalkCreateRequest를 파라미터로 받는 대신 필요한 정보들을 직접 받도록 변경
    suspend fun createSafeWalkSession(
        originLat: Double, // ✅ double 타입으로 위도 받기
        originLon: Double,
        originAddress: String,
        destinationLat: Double, // ✅ double 타입으로 위도 받기
        destinationLon: Double,
        destinationAddress: String,
        expectedArrival: LocalDateTime?,
        timerMinutes: Int?,
        guardianIds: List<Long>
    ): SafeWalkSession

    suspend fun uploadLocationTrack(sessionId: Long, lat: Double, lon: Double, accuracy: Double): Boolean
    suspend fun endSafeWalkSession(sessionId: Long, reason: SessionEndReason): Boolean
    suspend fun getSafeWalkDetail(sessionId: Long): SafeWalkDetail
    suspend fun getCurrentSafeWalk(): SafeWalkSessionState?
    suspend fun getTracks(sessionId: Long, cursor: String?, size: Int, order: String): List<TrackItem>
}