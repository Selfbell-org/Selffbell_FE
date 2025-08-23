package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class EmergencyAlertResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageId")
    val messageId: String? = null
)
