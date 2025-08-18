package com.selfbell.data.api.response

data class ContactResponseDto(
    val contactId: Long,
    val me: ContactUserDto,
    val other: ContactUserDto,
    val status: String,
    val sharePermission: Boolean
)

data class ContactUserDto(
    val phoneNumber: String,
    val name: String
)