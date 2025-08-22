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

        // ✅ 디버깅을 위한 로그 추가
        Log.d("SafeWalkRepository", "SafeWalk 세션 생성 요청")
        Log.d("SafeWalkRepository", "Request Body: $requestBody")

        return try {
            val response = api.createSafeWalkSession(requestBody)
            response.toDomainModel()
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "SafeWalk 세션 생성 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    Log.e("SafeWalkRepository", "HTTP 에러 코드: ${e.code()}")
                    Log.e("SafeWalkRepository", "에러 응답 헤더: ${e.response()?.headers()}")
                    Log.e("SafeWalkRepository", "에러 응답: ${e.response()?.errorBody()?.string()}")
                    Log.e("SafeWalkRepository", "요청 URL: ${e.response()?.raw()?.request?.url}")
                }
                else -> {
                    Log.e("SafeWalkRepository", "기타 에러: ${e.message}")
                    Log.e("SafeWalkRepository", "에러 타입: ${e.javaClass.simpleName}")
                }
            }
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
        // 디버깅을 위한 로그 추가
        Log.d("SafeWalkRepository", "위치 트랙 업로드 요청: sessionId=$sessionId, requestBody=$requestBody")
        
        // 현재 세션 상태 확인
        try {
            val currentSession = api.getCurrentSafeWalk()
            Log.d("SafeWalkRepository", "현재 세션 상태: $currentSession")
            
            // 세션 상세 정보 조회로 소유자 확인
            try {
                val sessionDetail = api.getSafeWalkDetail(sessionId)
                Log.d("SafeWalkRepository", "세션 상세 정보: $sessionDetail")
            } catch (e: Exception) {
                Log.w("SafeWalkRepository", "세션 상세 정보 조회 실패", e)
            }
        } catch (e: Exception) {
            Log.w("SafeWalkRepository", "현재 세션 상태 확인 실패", e)
        }
        
        return try {
            val response = api.uploadLocationTrack(sessionId, requestBody)
            Log.d("SafeWalkRepository", "위치 트랙 업로드 성공: ${response.status}")
            response.status == "UPLOADED"
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "위치 트랙 업로드 실패", e)
            when (e) {
                is retrofit2.HttpException -> {
                    Log.e("SafeWalkRepository", "위치 트랙 HTTP 에러: ${e.code()}")
                    Log.e("SafeWalkRepository", "위치 트랙 에러 응답 헤더: ${e.response()?.headers()}")
                    Log.e("SafeWalkRepository", "위치 트랙 에러 응답: ${e.response()?.errorBody()?.string()}")
                    Log.e("SafeWalkRepository", "요청 URL: ${e.response()?.raw()?.request?.url}")
                    
                    // 403 에러 시 추가 디버깅
                    if (e.code() == 403) {
                        Log.e("SafeWalkRepository", "=== 403 권한 에러 상세 분석 ===")
                        Log.e("SafeWalkRepository", "세션 ID: $sessionId")
                        Log.e("SafeWalkRepository", "현재 사용자 토큰: ${e.response()?.raw()?.request?.header("Authorization")}")
                        
                        // 세션 소유자와 현재 사용자 비교
                        try {
                            val sessionDetail = api.getSafeWalkDetail(sessionId)
                            Log.e("SafeWalkRepository", "세션 소유자 정보: ${sessionDetail.ward}")
                            Log.e("SafeWalkRepository", "세션 상태: ${sessionDetail.status}")
                        } catch (detailError: Exception) {
                            Log.e("SafeWalkRepository", "세션 상세 조회 실패: $detailError")
                        }
                    }
                }
            }
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
                    Log.e("SafeWalkRepository", "세션 종료 HTTP 에러: ${e.code()}")
                    Log.e("SafeWalkRepository", "세션 종료 에러 응답: ${e.response()?.errorBody()?.string()}")
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
                    Log.e("SafeWalkRepository", "세션 상세 조회 HTTP 에러: ${e.code()}")
                    Log.e("SafeWalkRepository", "세션 상세 조회 에러 응답: ${e.response()?.errorBody()?.string()}")
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
                    Log.e("SafeWalkRepository", "현재 세션 조회 에러 응답: ${e.response()?.errorBody()?.string()}")
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
                    Log.e("SafeWalkRepository", "트랙 조회 에러 응답: ${e.response()?.errorBody()?.string()}")
                }
                else -> {
                    Log.e("SafeWalkRepository", "기타 에러: ${e.message}")
                    Log.e("SafeWalkRepository", "에러 타입: ${e.javaClass.simpleName}")
                }
            }
            throw e
        }
    }
    // ✅ 히스토리 조회 함수 추가
    override suspend fun getSafeWalkHistory(filter: HistoryFilter): List<SafeWalkHistoryItem> {
        return try {
            val response = api.getSafeWalkHistory(
                userType = filter.userType.name,
                dateRange = filter.dateRange.name,
                sortOrder = filter.sortOrder.name
            )

            if (response.isSuccessful) {
                val historyResponse = response.body() ?: throw Exception("응답 본문이 비어있습니다.")
                historyResponse.items.map { it.toDomainModel() }
            } else {
                throw Exception("API 호출 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SafeWalkRepository", "히스토리 조회 실패", e)
            throw e
        }
    }
}