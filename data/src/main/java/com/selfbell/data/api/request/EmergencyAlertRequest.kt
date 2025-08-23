package com.selfbell.data.api.request

import com.google.gson.annotations.SerializedName

data class EmergencyAlertRequest(
    @SerializedName("recipientToken")
    val recipientToken: String,
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("title")
    val title: String = "긴급 상황 문자가 도착했습니다",
    @SerializedName("message")
    val message: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)
