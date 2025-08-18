package com.selfbell.domain.model

data class ContactRelationship(
    val contactId: Long,
    val other: ContactUser,
    val status: String,
    val sharePermission: Boolean
)

data class ContactUser(
    val phoneNumber: String,
    val name: String
)