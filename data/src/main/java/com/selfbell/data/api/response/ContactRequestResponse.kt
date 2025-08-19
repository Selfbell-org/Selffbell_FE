package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class ContactRequestResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String? = null
) 