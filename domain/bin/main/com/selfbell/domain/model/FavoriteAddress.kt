package com.selfbell.domain.model

data class FavoriteAddress(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
)