package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class SafeWalkCreateRequest(
    val origin: LocationRequest,
    val originAddress: String,
    val destination: LocationRequest,
    val destinationAddress: String,
    val expectedArrival: String? = null,
    val timerMinutes: Int? = null,
    val guardianIds: List<Long>
)

data class LocationRequest(val lat: Double, val lon: Double)

data class SafeWalkCreateResponse(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("safeWalkStatus") val status: String,
    @SerializedName("startedAt") val startedAt: String,
    @SerializedName("expectedArrival") val expectedArrival: String? = null,
    @SerializedName("timerEnd") val timerEnd: String? = null,
    @SerializedName("topic") val topic: String
)

data class TrackRequest(
    val lat: Double,
    val lon: Double,
    val accuracyM: Double,
    val capturedAt: String
)

data class TrackResponse(
    @SerializedName("trackId") val trackId: Long,
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("accuracyM") val accuracyM: Double,
    @SerializedName("capturedAt") val capturedAt: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("status") val status: String
)

data class EndRequest(
    val reason: String
)

data class EndResponse(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("endedAt") val endedAt: String
)

data class SafeWalkDetailResponse(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("ward") val ward: WardResponse,
    @SerializedName("origin") val origin: LocationDetailResponse,
    @SerializedName("destination") val destination: LocationDetailResponse,
    @SerializedName("status") val status: String,
    @SerializedName("startedAt") val startedAt: String,
    @SerializedName("expectedArrival") val expectedArrival: String? = null,
    @SerializedName("timerEnd") val timerEnd: String? = null,
    @SerializedName("guardians") val guardians: List<GuardianResponse>
)
data class WardResponse(
    @SerializedName("id") val id: Long, 
    @SerializedName("name") val name: String
)
data class LocationDetailResponse(
    @SerializedName("lat") val lat: Double, 
    @SerializedName("lon") val lon: Double, 
    @SerializedName("addressText") val addressText: String
)
data class GuardianResponse(
    @SerializedName("id") val id: Long, 
    @SerializedName("name") val name: String
)

data class CurrentSafeWalkResponse(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("topic") val topic: String
)

data class TracksResponse(
    @SerializedName("items") val items: List<TrackItemResponse>,
    @SerializedName("nextCursor") val nextCursor: String?,
    @SerializedName("count") val count: Int
)
data class TrackItemResponse(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("accuracyM") val accuracyM: Double,
    @SerializedName("capturedAt") val capturedAt: String
)