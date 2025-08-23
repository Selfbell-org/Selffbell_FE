package com.selfbell.data.api.response

// 네이버 Reverse Geocoding API의 응답 구조에 맞춘 DTO
data class NaverReverseGeocodeResponse(
    val results: List<GeocodeResult>?
)

data class GeocodeResult(
    val name: String,
    val region: GeocodeRegion,
    val land: GeocodeLand?
)

data class GeocodeRegion(
    val area1: GeocodeArea, // 시/도
    val area2: GeocodeArea, // 시/군/구
    val area3: GeocodeArea  // 읍/면/동
)

data class GeocodeLand(
    val name: String?, // 도로명 또는 지번
    val number1: String?, // 건물번호 1
    val number2: String? // 건물번호 2
)

data class GeocodeArea(
    val name: String
)