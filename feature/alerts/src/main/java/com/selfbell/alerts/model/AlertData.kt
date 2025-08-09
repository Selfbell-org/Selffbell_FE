// feature/alerts/model/AlertData.kt
package com.selfbell.alerts.model

import com.naver.maps.geometry.LatLng
import com.selfbell.core.R // core 모듈의 R 파일 경로

data class AlertData(
    val id: Int,
    val latLng: LatLng,
    val address: String,
    val distance: Int,
    val type: AlertType
) {
    fun getIconResource(): Int {
        return when (type) {
            AlertType.EMERGENCY_CALL -> R.drawable.alerts_icon // 긴급신고 아이콘
            AlertType.CRIMINAL_INFO -> R.drawable.crime_pin_icon // 범죄자 아이콘
        }
    }
}

enum class AlertType {
    EMERGENCY_CALL,
    CRIMINAL_INFO
}