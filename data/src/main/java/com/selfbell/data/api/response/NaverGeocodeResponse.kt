package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class NaverGeocodeResponse(
    @SerializedName("status")
    val status: String?, // "OK", "INVALID_REQUEST", "SYSTEM_ERROR" 등

    @SerializedName("meta")
    val meta: Meta?,

    @SerializedName("addresses")
    val addresses: List<AddressResponse>?,

    @SerializedName("errorMessage")
    val errorMessage: String?
)

data class Meta(
    @SerializedName("totalCount")
    val totalCount: Int?,

    @SerializedName("page")
    val page: Int?,

    @SerializedName("count")
    val count: Int?
)

data class AddressResponse(
    @SerializedName("roadAddress")
    val roadAddress: String?,

    @SerializedName("jibunAddress")
    val jibunAddress: String?,

    @SerializedName("englishAddress")
    val englishAddress: String?,

    @SerializedName("addressElements")
    val addressElements: List<AddressElement>?,

    @SerializedName("x")
    val x: String?,

    @SerializedName("y")
    val y: String?,

    @SerializedName("distance")
    val distance: Double?
)

data class AddressElement(
    @SerializedName("types")
    val types: List<String>?,

    @SerializedName("longName")
    val longName: String?,

    @SerializedName("shortName")
    val shortName: String?,

    @SerializedName("code") // 주소 요소 코드 (선택적)
    val code: String?
)
