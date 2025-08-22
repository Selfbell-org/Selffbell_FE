package com.selfbell.data.mapper

import com.selfbell.data.api.response.*
import com.selfbell.domain.model.*
import java.time.LocalDateTime

fun SafeWalkCreateResponse.toDomainModel(): SafeWalkSession {
    return SafeWalkSession(
        sessionId = this.sessionId,
        status = SafeWalkStatus.valueOf(this.status),
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
        expectedArrival = this.expectedArrival?.let { LocalDateTime.parse(it) },
        timerEnd = this.timerEnd?.let { LocalDateTime.parse(it) },
        guardians = this.guardians.map { it.toDomainModel() },
        endedAt = null // endedAt 필드가 응답에 없으므로 일단 null로 처리합니다.
    )
}

fun WardResponse.toDomainModel(): Ward {
    return Ward(
        id = this.id,
        nickname = this.nickname
    )
}

fun GuardianResponse.toDomainModel(): Guardian {
    return Guardian(
        id = this.id,
        nickname = this.nickname
    )
}

// ✅ LocationDetailResponse는 LocationDetail 도메인 모델로 매핑합니다.
fun LocationDetailResponse.toDomainModel(): LocationDetail {
    return LocationDetail(
        lat = this.lat,
        lon = this.lon,
        addressText = this.addressText
    )
}

fun SafeWalkHistoryItemResponse.toDomainModel(): SafeWalkHistoryItem {
    return SafeWalkHistoryItem(
        id = this.sessionId,
        userProfileUrl = this.targetUser.profileImageUrl,
        userName = this.targetUser.name,
        userType = this.type,
        destinationName = this.destinationAddress,
        dateTime = LocalDateTime.parse(this.startedAt),
        status = SafeWalkStatus.valueOf(this.status)
    )
}