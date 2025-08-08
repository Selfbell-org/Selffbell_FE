package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class NaverGeocodeResponse(
    val status: String,
    val meta: Meta,
    val addresses: List<AddressResponse>
)

data class Meta(
    val totalCount: Int,
    val page: Int,
    val count: Int
)

data class AddressResponse(
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
    val addressElements: List<AddressElement>,
    val x: String,
    val y: String,
    val distance: Double
)

data class AddressElement(
    val types: List<String>,
    val longName: String,
    val shortName: String,
    val code: String
)