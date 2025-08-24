package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("exists")
    val exists: Boolean
)
