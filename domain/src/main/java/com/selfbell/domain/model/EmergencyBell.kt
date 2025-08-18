package com.selfbell.domain.model

data class EmergencyBell(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val detail: String,
    val distance: Double
)

data class EmergencyBellDetail(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val detail: String,
    val managerTel: String,
    val address: String,
    val type: String,
    val distance: Double
)
