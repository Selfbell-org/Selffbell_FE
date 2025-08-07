package com.selfbell.home.model

import com.naver.maps.geometry.LatLng

data class MapMarkerData(
    val latLng: LatLng,
    val address: String, // 말풍선에 표기될 주소
    val type: MarkerType
)
enum class MarkerType { USER, CRIMINAL, SAFETY_BELL }

