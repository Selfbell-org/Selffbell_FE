package com.selfbell.data.api.request

data class SosMessageRequest(
    val receiverUserIds: List<Long>,
    val templateId: Int,
    val message: String,
    val lat: Double,
    val lon: Double
)
