package com.selfbell.data.repository.impl

import com.selfbell.data.api.EmergencyBellApi
import com.selfbell.data.api.response.EmergencyBellDetailResponse
import com.selfbell.data.api.response.EmergencyBellNearby
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.repository.EmergencyBellRepository
import javax.inject.Inject

class EmergencyBellRepositoryImpl @Inject constructor(
    private val api: EmergencyBellApi
) : EmergencyBellRepository {

    override suspend fun getNearbyEmergencyBells(lat: Double, lon: Double, radius: Int): List<EmergencyBell> {
        return api.getNearbyEmergencyBells(lat, lon, radius).items.map { it.toDomainModel() }
    }

    override suspend fun getEmergencyBellDetail(id: Int): EmergencyBellDetail {
        return api.getEmergencyBellDetail(id).toDomainModel()
    }
}