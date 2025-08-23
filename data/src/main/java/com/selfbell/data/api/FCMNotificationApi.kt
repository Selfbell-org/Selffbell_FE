package com.selfbell.data.api

import com.selfbell.data.api.request.EmergencyAlertRequest
import com.selfbell.data.api.response.EmergencyAlertResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FCMNotificationApi {
    
    @POST("api/v1/notifications/emergency-alert")
    suspend fun sendEmergencyAlert(
        @Body request: EmergencyAlertRequest
    ): EmergencyAlertResponse
}
