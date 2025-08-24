package com.selfbell.data.repository.impl

import com.selfbell.data.api.EmergencyBellApi
import com.selfbell.data.api.FCMNotificationApi
import com.selfbell.data.api.request.EmergencyAlertRequest
import com.selfbell.data.api.request.SosMessageRequest as DataSosMessageRequest
import com.selfbell.data.api.response.EmergencyBellDetailResponse
import com.selfbell.data.api.response.EmergencyBellNearby
import com.selfbell.data.api.response.SosMessageResponse as DataSosMessageResponse
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.model.SosMessageRequest
import com.selfbell.domain.model.SosMessageResponse
import com.selfbell.domain.repository.EmergencyBellRepository
import javax.inject.Inject
import android.util.Log

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
    
    override suspend fun sendSosMessage(request: SosMessageRequest): SosMessageResponse {
        Log.d("EmergencyBellRepositoryImpl", "=== sendSosMessage 시작 ===")
        Log.d("EmergencyBellRepositoryImpl", "입력받은 Domain 모델: $request")
        
        // Domain 모델을 Data 모델로 변환
        Log.d("EmergencyBellRepositoryImpl", "Domain → Data 모델 변환 시작")
        val dataRequest = DataSosMessageRequest(
            receiverUserIds = request.receiverUserIds,
            templateId = request.templateId,
            message = request.message,
            lat = request.lat,
            lon = request.lon
        )
        Log.d("EmergencyBellRepositoryImpl", "변환된 Data 모델: $dataRequest")
        
        // API 호출
        Log.d("EmergencyBellRepositoryImpl", "EmergencyBellApi.sendSosMessage() 호출")
        val dataResponse = api.sendSosMessage(dataRequest)
        Log.d("EmergencyBellRepositoryImpl", "API 응답 수신: $dataResponse")
        
        // Data 응답을 Domain 응답으로 변환
        Log.d("EmergencyBellRepositoryImpl", "Data → Domain 모델 변환")
        val domainResponse = SosMessageResponse(
            id = dataResponse.id,
            sentCount = dataResponse.sentCount
        )
        Log.d("EmergencyBellRepositoryImpl", "변환된 Domain 모델: $domainResponse")
        
        Log.d("EmergencyBellRepositoryImpl", "=== sendSosMessage 완료 ===")
        return domainResponse
    }
}