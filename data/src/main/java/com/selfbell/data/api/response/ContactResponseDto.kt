package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

data class ContactResponseDto(
    val contactId: Long,
    val me: ContactUserDto? = null,
    val other: ContactUserDto,
    val status: String,
    val sharePermission: Boolean
)

data class ContactUserDto(
    @SerializedName("userId")
    val userId: Long? = null,
    val phoneNumber: String,
    val name: String
)