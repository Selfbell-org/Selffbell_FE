package com.selfbell.data.repository.impl

import com.google.android.gms.maps.model.LatLng
import com.selfbell.data.api.SafeWalksApi
import com.selfbell.data.api.response.*
import com.selfbell.data.mapper.*
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SafeWalkRepositoryImpl @Inject constructor(
    private val api: SafeWalksApi
) : SafeWalkRepository {

    // ✅ 파라미터로 받은 정보들을 사용해 SafeWalkCreateRequest를 생성
    override suspend fun createSafeWalkSession(
        originLat: Double, // ✅ double 타입으로 변경
        originLon: Double, // ✅ double 타입으로 변경
        originAddress: String,
        destinationLat: Double, // ✅ double 타입으로 변경
        destinationLon: Double,
        destinationAddress: String,
        expectedArrival: LocalDateTime?,
        timerMinutes: Int?,
        guardianIds: List<Long>
    ): SafeWalkSession {
        val requestBody = SafeWalkCreateRequest(
            origin = LocationRequest(originLat, originLon),
            originAddress = originAddress,
            destination = LocationRequest(destinationLat, destinationLon),
            destinationAddress = destinationAddress,
            expectedArrival = expectedArrival?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            timerMinutes = timerMinutes,
            guardianIds = guardianIds
        )
        return api.createSafeWalkSession(requestBody).toDomainModel()
    }
    override suspend fun uploadLocationTrack(sessionId: Long, lat: Double, lon: Double, accuracy: Double): Boolean {
        val requestBody = TrackRequest(
            lat = lat,
            lon = lon,
            accuracyM = accuracy,
            capturedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        return try {
            val response = api.uploadLocationTrack(sessionId, requestBody)
            response.status == "UPLOADED"
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun endSafeWalkSession(sessionId: Long, reason: SessionEndReason): Boolean {
        return try {
            val requestBody = EndRequest(reason = reason.name)
            val response = api.endSafeWalkSession(sessionId, requestBody)
            response.status.contains("END")
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSafeWalkDetail(sessionId: Long): SafeWalkDetail {
        return api.getSafeWalkDetail(sessionId).toDomainModel()
    }

    override suspend fun getCurrentSafeWalk(): SafeWalkSessionState? {
        return try {
            api.getCurrentSafeWalk()?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTracks(sessionId: Long, cursor: String?, size: Int, order: String): List<TrackItem> {
        return api.getTracks(sessionId, cursor, size, order).items.map { it.toDomainModel() }
    }
}