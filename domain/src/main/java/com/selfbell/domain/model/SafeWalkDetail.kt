package com.selfbell.domain.model

import java.time.LocalDateTime

// ✅ 1. 안심 귀가 세션의 기본 정보
data class SafeWalkSession(
    val sessionId: Long,
    val status: String,
    val startedAt: LocalDateTime,
    val expectedArrival: LocalDateTime?,
    val timerEnd: LocalDateTime?,
    val topic: String
)

// ✅ 2. 현재 진행 중인 세션 상태
data class SafeWalkSessionState(
    val sessionId: Long,
    val status: String,
    val topic: String
)

// ✅ 3. 트랙킹 아이템
data class TrackItem(
    val lat: Double,
    val lon: Double,
    val accuracy: Double,
    val capturedAt: LocalDateTime
)

// ✅ 4. 안심 귀가 세션의 상세 정보
data class SafeWalkDetail(
    val sessionId: Long,
    val ward: Ward,
    val origin: LocationDetail,
    val destination: LocationDetail,
    val status: SafeWalkStatus,
    val startedAt: LocalDateTime,
    val expectedArrival: LocalDateTime?,
    val timerEnd: LocalDateTime?,
    val guardians: List<Guardian>,
    val endedAt: LocalDateTime?,
    // ✅ 서버에서 받아올 추가 필드들
    val expectedStartTime: LocalDateTime? = null, // 상대가 설정한 시작 시간
    val expectedEndTime: LocalDateTime? = null,   // 상대가 설정한 종료 시간
    val estimatedDurationMinutes: Int? = null,   // 예상 소요 시간 (분)
    val actualDurationMinutes: Int? = null,      // 실제 소요 시간 (분)
    val timeDifferenceMinutes: Int? = null       // 예상 대비 실제 시간 차이 (분)
)

// ✅ 5. 상세 정보에 사용되는 부가 모델
data class Ward(val id: Long, val nickname: String)
data class Guardian(val id: Long, val nickname: String)
data class LocationDetail(val lat: Double, val lon: Double, val addressText: String)

// ✅ 6. 상태를 정의하는 Enum
enum class SafeWalkStatus {
    IN_PROGRESS, // 귀가중
    COMPLETED,   // 완료
    CANCELED,    // 취소됨
    ENDED        // 종료됨
}

// ✅ 7. 세션 종료 이유
enum class SessionEndReason { MANUAL, ARRIVED, TIMEOUT }
