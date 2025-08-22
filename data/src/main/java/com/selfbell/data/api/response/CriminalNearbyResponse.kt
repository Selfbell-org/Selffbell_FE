package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class CriminalNearbyResponse(
    @SerializedName("address")
    val address: String,
    
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lon")
    val lon: Double,
    
    @SerializedName("distanceMeters")
    val distanceMeters: Double
)
