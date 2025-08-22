package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class FavoriteAddressListResponse(
    @SerializedName("items") val items: List<FavoriteAddressItem>
)

data class FavoriteAddressItem(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)