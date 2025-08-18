package com.selfbell.domain.model

import java.time.LocalDateTime

data class SafeWalkSession(
    val sessionId: Long,
    val status: String,
    val startedAt: LocalDateTime,
    val expectedArrival: LocalDateTime?,
    val timerEnd: LocalDateTime?,
    val topic: String
)

data class SafeWalkSessionState(
    val sessionId: Long,
    val status: String,
    val topic: String
)

data class TrackItem(
    val lat: Double,
    val lon: Double,
    val accuracy: Double,
    val capturedAt: LocalDateTime
)

data class SafeWalkDetail(
    val sessionId: Long,
    val ward: Ward,
    val origin: Location,
    val destination: Location,
    val status: String,
    val startedAt: LocalDateTime,
    val expectedArrival: LocalDateTime?,
    val timerEnd: LocalDateTime?,
    val guardians: List<Guardian>
)

data class Ward(val id: Long, val nickname: String)
data class Guardian(val id: Long, val nickname: String)
data class Location(val lat: Double, val lon: Double, val addressText: String)

enum class SessionEndReason { MANUAL, ARRIVED, TIMEOUT }

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)