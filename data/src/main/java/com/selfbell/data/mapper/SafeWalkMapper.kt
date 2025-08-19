package com.selfbell.data.mapper

import com.selfbell.data.api.response.*
import com.selfbell.domain.model.*
import java.time.LocalDateTime
import com.selfbell.domain.model.Location as DomainLocation

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
        status = this.status,
        startedAt = LocalDateTime.parse(this.startedAt),
        expectedArrival = this.expectedArrival?.let { LocalDateTime.parse(it) },
        timerEnd = this.timerEnd?.let { LocalDateTime.parse(it) },
        guardians = this.guardians.map { it.toDomainModel() }
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

fun LocationDetailResponse.toDomainModel(): DomainLocation {
    return DomainLocation(
        lat = this.lat,
        lon = this.lon,
        addressText = this.addressText
    )
}