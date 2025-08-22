package com.selfbell.domain.repository

import com.selfbell.domain.model.*
import java.time.LocalDateTime

interface SafeWalkRepository {
    suspend fun createSafeWalkSession(
        originLat: Double,
        originLon: Double,
        originAddress: String,
        destinationLat: Double,
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

    /**
     * ✅ 히스토리 목록을 가져오는 함수 추가
     * @param filter 필터링 옵션 (사용자 유형, 기간, 정렬 순서)
     * @return 필터링된 히스토리 목록
     */
    suspend fun getSafeWalkHistory(filter: HistoryFilter): List<SafeWalkHistoryItem>
}