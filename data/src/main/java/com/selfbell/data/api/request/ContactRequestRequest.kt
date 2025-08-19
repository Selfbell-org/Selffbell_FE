package com.selfbell.data.api.request

import com.google.gson.annotations.SerializedName

data class ContactRequestRequest(
    @SerializedName("toPhoneNumber") val toPhoneNumber: String
) 