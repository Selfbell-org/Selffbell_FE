package com.selfbell.domain.repository

import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail

interface EmergencyBellRepository {
    suspend fun getNearbyEmergencyBells(lat: Double, lon: Double, radius: Int): List<EmergencyBell>
    suspend fun getEmergencyBellDetail(id: Int): EmergencyBellDetail
    suspend fun sendEmergencyAlert(
        recipientToken: String,
        senderId: String,
        message: String,
        lat: Double,
        lon: Double
    )
}