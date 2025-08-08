package com.selfbell.home.model

import androidx.annotation.DrawableRes
import com.naver.maps.geometry.LatLng
import com.selfbell.feature.home.R

data class MapMarkerData(
    val latLng: LatLng,
    val address: String, // 말풍선에 표기될 주소
    val type: MarkerType,
    val distance: String
) {
    // 마커 타입에 따라 아이콘 리소스 반환하는 함수 (예시)
    @DrawableRes
    fun getIconResource(): Int {
        return when (type) {
            MarkerType.USER -> R.drawable.user_marker_icon // 사용자 위치 마커 (예시)
            MarkerType.CRIMINAL -> R.drawable.criminal_icon
            MarkerType.SAFETY_BELL -> R.drawable.sos_icon // 안심벨 아이콘을 sos_icon으로 사용한다고 가정
        }
    }
enum class MarkerType { USER, CRIMINAL, SAFETY_BELL }}

