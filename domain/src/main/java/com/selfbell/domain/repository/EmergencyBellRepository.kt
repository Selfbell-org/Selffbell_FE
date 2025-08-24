package com.selfbell.domain.repository

import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.model.SosMessageRequest
import com.selfbell.domain.model.SosMessageResponse

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
    
    suspend fun sendSosMessage(request: SosMessageRequest): SosMessageResponse
}