package com.selfbell.domain.model

data class CriminalDetail(
    val address: String,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double,
    val name: String? = null,
    val crimeType: String? = null,
    val registrationDate: String? = null,
    val releaseDate: String? = null,
    val age: Int? = null,
    val gender: String? = null
)
