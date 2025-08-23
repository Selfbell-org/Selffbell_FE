package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class FCMTokenResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("fcmToken")
    val fcmToken: String?,
    @SerializedName("success")
    val success: Boolean
)
