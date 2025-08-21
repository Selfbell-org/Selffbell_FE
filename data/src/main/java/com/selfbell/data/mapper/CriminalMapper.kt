package com.selfbell.data.mapper

import com.selfbell.data.api.response.CriminalNearbyResponse
import com.selfbell.domain.model.Criminal

fun CriminalNearbyResponse.toDomainModel(): Criminal {
    return Criminal(
        address = this.address,
        lat = this.lat,
        lon = this.lon,
        distanceMeters = this.distanceMeters
    )
}
