package com.selfbell.data.repository.impl

import com.selfbell.data.api.EmergencyBellApi
import com.selfbell.data.api.FCMNotificationApi
import com.selfbell.data.api.request.EmergencyAlertRequest
import com.selfbell.data.api.response.EmergencyBellDetailResponse
import com.selfbell.data.api.response.EmergencyBellNearby
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.repository.EmergencyBellRepository
import javax.inject.Inject

class EmergencyBellRepositoryImpl @Inject constructor(
    private val api: EmergencyBellApi,
    private val fcmNotificationApi: FCMNotificationApi
) : EmergencyBellRepository {

    override suspend fun getNearbyEmergencyBells(lat: Double, lon: Double, radius: Int): List<EmergencyBell> {
        return api.getNearbyEmergencyBells(lat, lon, radius).items.map { it.toDomainModel() }
    }

    override suspend fun getEmergencyBellDetail(objt_id: Int): EmergencyBellDetail {
        return api.getEmergencyBellDetail(objt_id).toDomainModel()
    }

    override suspend fun sendEmergencyAlert(
        recipientToken: String,
        senderId: String,
        message: String,
        lat: Double,
        lon: Double
    ) {
        val request = EmergencyAlertRequest(
            recipientToken = recipientToken,
            senderId = senderId,
            title = "긴급 상황 문자가 도착했습니다",
            message = message,
            lat = lat,
            lon = lon
        )
        
        fcmNotificationApi.sendEmergencyAlert(request)
    }
}