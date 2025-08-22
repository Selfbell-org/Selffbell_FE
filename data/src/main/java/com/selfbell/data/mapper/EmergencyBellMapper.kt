package com.selfbell.data.mapper

import com.selfbell.data.api.response.EmergencyBellDetailResponse
import com.selfbell.data.api.response.EmergencyBellNearby
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail

fun EmergencyBellNearby.toDomainModel(): EmergencyBell {
    return EmergencyBell(
        id = this.objtId,
        lat = this.lat,
        lon = this.lon,
        detail = this.insDetail,
        distance = this.distance // null일 수 있음
    )
}

fun EmergencyBellDetailResponse.toDomainModel(): EmergencyBellDetail {
    return EmergencyBellDetail(
        id = this.objtId,
        lat = this.lat,
        lon = this.lon,
        detail = this.insDetail,
        managerTel = this.mngTel,
        address = this.adres,
        type = this.insType,
        distance = this.distance // null일 수 있음
    )
}