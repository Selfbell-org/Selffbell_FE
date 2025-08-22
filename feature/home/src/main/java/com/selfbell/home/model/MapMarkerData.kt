package com.selfbell.home.model

import androidx.annotation.DrawableRes
import com.naver.maps.geometry.LatLng
import com.selfbell.feature.home.R

data class MapMarkerData(
    val latLng: LatLng,
    val address: String, // 마커 클릭 시 말풍선에 표기될 주소
    val type: MarkerType,
    val distance: Double, // ✅ Double 타입으로 변경
    val objtId: Int? = null, // ✅ 안심벨 상세 조회를 위한 ID
    val insDetail: String? = null // ✅ 안심벨의 상세 명칭
) {
    // 마커 타입에 따라 아이콘 리소스 반환하는 함수
    @DrawableRes
    fun getIconResource(): Int {
        return when (type) {
            MarkerType.USER -> R.drawable.user_marker_icon
            MarkerType.CRIMINAL -> R.drawable.criminal_icon
            MarkerType.SAFETY_BELL -> R.drawable.sos_icon
        }
    }


    enum class MarkerType {
        USER,
        CRIMINAL,
        SAFETY_BELL
    }
}