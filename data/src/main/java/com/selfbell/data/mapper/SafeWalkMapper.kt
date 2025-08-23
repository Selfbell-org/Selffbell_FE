package com.selfbell.data.mapper

import com.selfbell.data.api.response.*
import com.selfbell.domain.model.*
import java.time.LocalDateTime

fun SafeWalkCreateResponse.toDomainModel(): SafeWalkSession {
    return SafeWalkSession(
        sessionId = this.sessionId,
        status = this.status,
        startedAt = LocalDateTime.parse(this.startedAt),
        expectedArrival = this.expectedArrival?.let { LocalDateTime.parse(it) },
        timerEnd = this.timerEnd?.let { LocalDateTime.parse(it) },
        topic = this.topic
    )
}

fun CurrentSafeWalkResponse.toDomainModel(): SafeWalkSessionState {
    return SafeWalkSessionState(
        sessionId = this.sessionId,
        status = this.status,
        topic = this.topic
    )
}

fun TrackItemResponse.toDomainModel(): TrackItem {
    return TrackItem(
        lat = this.lat,
        lon = this.lon,
        accuracy = this.accuracyM,
        capturedAt = LocalDateTime.parse(this.capturedAt)
    )
}

fun SafeWalkDetailResponse.toDomainModel(): SafeWalkDetail {
    return SafeWalkDetail(
        sessionId = this.sessionId,
        ward = this.ward.toDomainModel(),
        origin = this.origin.toDomainModel(),
        destination = this.destination.toDomainModel(),
        status = SafeWalkStatus.valueOf(this.status),
        startedAt = LocalDateTime.parse(this.startedAt),
        endedAt = this.endedAt?.let { LocalDateTime.parse(it) },
        expectedArrival = this.expectedArrival?.let { LocalDateTime.parse(it) },
        timerEnd = this.timerEnd?.let { LocalDateTime.parse(it) },
        guardians = this.guardians.map { it.toDomainModel() }
    )
}

fun WardResponse.toDomainModel(): Ward {
    return Ward(
        id = this.id,
        nickname = this.name
    )
}

fun GuardianResponse.toDomainModel(): Guardian {
    return Guardian(
        id = this.id,
        nickname = this.name
    )
}

fun LocationDetailResponse.toDomainModel(): LocationDetail {
    return LocationDetail(
        lat = this.lat,
        lon = this.lon,
        addressText = this.addressText
    )
}

// ✅ [추가] HistorySessionDto를 SafeWalkHistoryItem으로 변환하는 Mapper 함수
fun HistorySessionDto.toDomainModel(): SafeWalkHistoryItem {
    return SafeWalkHistoryItem(
        sessionId = this.session.id,
        wardName = this.ward.name,
        destinationName = this.session.destinationName,
        startedAt = LocalDateTime.parse(this.session.startedAt),
        status = SafeWalkStatus.fromString(this.session.status)
    )
}
