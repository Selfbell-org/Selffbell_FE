package com.selfbell.data.repository.impl

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.selfbell.data.api.SafeWalksApi
import com.selfbell.data.api.response.*
import com.selfbell.data.mapper.*
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.selfbell.data.mapper.toDomainModel

class SafeWalkRepositoryImpl @Inject constructor(
    private val api: SafeWalksApi
) : SafeWalkRepository {

    // ✅ 파라미터로 받은 정보들을 사용해 SafeWalkCreateRequest를 생성
    override suspend fun createSafeWalkSession(
        originLat: Double,
        originLon: Double,
        originAddress: String,
        destinationLat: Double,
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

        return try {
            val response = api.createSafeWalkSession(requestBody)
            response.toDomainModel()
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "SafeWalk 세션 생성 실패", e)
            throw e
        }
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
            Log.e("SafeWalkRepository", "위치 트랙 업로드 실패", e)
            false
        }
    }

    override suspend fun endSafeWalkSession(sessionId: Long, reason: SessionEndReason): Boolean {
        return try {
            val requestBody = EndRequest(reason = reason.name)
            val response = api.endSafeWalkSession(sessionId, requestBody)
            Log.d("SafeWalkRepository", "세션 종료 성공: ${response.status}")
            response.status.contains("END")
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "세션 종료 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SafeWalkRepository", "세션 종료 HTTP 에러: ${e.code()}")
                    Log.e("SafeWalkRepository", "세션 종료 에러 응답: $errorBody")
                }
            }
            false
        }
    }

    override suspend fun getSafeWalkDetail(sessionId: Long): SafeWalkDetail {
        return try {
            val response = api.getSafeWalkDetail(sessionId)
            Log.d("SafeWalkRepository", "세션 상세 조회 성공: ${response.sessionId}")
            response.toDomainModel()
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "세션 상세 조회 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("SafeWalkRepository", "세션 상세 조회 HTTP 에러: ${e.code()}")
                    Log.e("SafeWalkRepository", "세션 상세 조회 에러 응답: $errorBody")
                }
            }
            throw e
        }
    }

    override suspend fun getCurrentSafeWalk(): SafeWalkSessionState? {
        return try {
            val response = api.getCurrentSafeWalk()
            if (response != null) {
                Log.d("SafeWalkRepository", "현재 세션 조회 성공: ${response.sessionId}")
            } else {
                Log.d("SafeWalkRepository", "현재 진행 중인 세션이 없습니다")
            }
            response?.toDomainModel()
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "현재 세션 조회 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    Log.e("SafeWalkRepository", "현재 세션 조회 HTTP 에러: ${e.code()}")
                }
            }
            null
        }
    }

    override suspend fun getTracks(sessionId: Long, cursor: String?, size: Int, order: String): List<TrackItem> {
        return try {
            val response = api.getTracks(sessionId, cursor, size, order)
            Log.d("SafeWalkRepository", "트랙 조회 성공: ${response.items.size}개")
            response.items.map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "트랙 조회 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    Log.e("SafeWalkRepository", "트랙 조회 HTTP 에러: ${e.code()}")
                }
                else -> {
                    Log.e("SafeWalkRepository", "기타 에러: ${e.message}")
                    Log.e("SafeWalkRepository", "에러 타입: ${e.javaClass.simpleName}")
                }
            }
            throw e
        }
    }
    // ✅ 히스토리 조회 함수 - 더미 데이터 사용
    override suspend fun getSafeWalkHistory(filter: HistoryFilter): List<SafeWalkHistoryItem> {
        return try {
            // 더미 데이터 생성
            val dummyHistoryItems = listOf(
                SafeWalkDetail(
                    sessionId = 1L,
                    ward = Ward(id = 13L, name = "사용자1"),
                    origin = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "출발지1"),
                    destination = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "도착지1"),
                    status = "COMPLETED",
                    startedAt = LocalDateTime.now().minusHours(2),
                    expectedArrival = LocalDateTime.now().minusHours(1),
                    timerEnd = LocalDateTime.now().minusHours(1),
                    guardians = listOf(Guardian(id = 5L, name = "보호자1"))
                ),
                SafeWalkDetail(
                    sessionId = 2L,
                    ward = Ward(id = 13L, name = "사용자1"),
                    origin = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "출발지2"),
                    destination = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "도착지2"),
                    status = "IN_PROGRESS",
                    startedAt = LocalDateTime.now().minusMinutes(30),
                    expectedArrival = LocalDateTime.now().plusMinutes(30),
                    timerEnd = LocalDateTime.now().plusMinutes(30),
                    guardians = listOf(Guardian(id = 5L, name = "보호자1"))
                ),
                SafeWalkDetail(
                    sessionId = 3L,
                    ward = Ward(id = 13L, name = "사용자1"),
                    origin = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "출발지3"),
                    destination = LocationDetail(lat = 37.5665, lon = 126.9780, addressText = "도착지3"),
                    status = "COMPLETED",
                    startedAt = LocalDateTime.now().minusDays(1),
                    expectedArrival = LocalDateTime.now().minusDays(1).plusHours(1),
                    timerEnd = LocalDateTime.now().minusDays(1).plusHours(1),
                    guardians = listOf(Guardian(id = 5L, name = "보호자1"))
                )
            )
            
            // 필터에 따라 정렬 및 필터링
            val filteredItems = when (filter.userType) {
                HistoryUserFilter.ALL -> dummyHistoryItems
                HistoryUserFilter.GUARDIANS -> dummyHistoryItems.filter { it.status == "IN_PROGRESS" }
                HistoryUserFilter.MINE -> dummyHistoryItems.filter { it.ward.id == 13L }
            }
            
            when (filter.sortOrder) {
                HistorySortOrder.LATEST -> filteredItems.sortedByDescending { it.startedAt }
                HistorySortOrder.OLDEST -> filteredItems.sortedBy { it.startedAt }
            }
            
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "히스토리 조회 실패", e)
            emptyList() // 에러 시 빈 리스트 반환
        }
    }
}