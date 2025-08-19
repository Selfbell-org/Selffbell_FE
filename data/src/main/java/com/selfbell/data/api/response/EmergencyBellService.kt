package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class EmergencyBellNearbyResponse(
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("items")
    val items: List<EmergencyBellNearby>
)

data class EmergencyBellNearby(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("ins_DETAIL")
    val insDetail: String,
    @SerializedName("objt_ID")
    val objtId: Int,
    @SerializedName("distance")
    val distance: Double? = null, // distance가 null일 수 있으므로 nullable로 변경
    @SerializedName("mng_TEL")
    val mngTel: String? = null, // 추가 필드
    @SerializedName("adres")
    val adres: String? = null, // 추가 필드
    @SerializedName("ins_TYPE")
    val insType: String? = null // 추가 필드
)

data class EmergencyBellDetailResponse(
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("ins_DETAIL")
    val insDetail: String,
    @SerializedName("objt_ID")
    val objtId: Int,
    @SerializedName("mng_TEL")
    val mngTel: String,
    @SerializedName("adres")
    val adres: String,
    @SerializedName("ins_TYPE")
    val insType: String,
    @SerializedName("distance")
    val distance: Double? = null // distance가 null일 수 있으므로 nullable로 변경
)
