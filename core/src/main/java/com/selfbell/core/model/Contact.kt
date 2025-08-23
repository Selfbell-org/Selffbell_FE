package com.selfbell.core.model

data class Contact(
    val id: Long,
    val userId: Long?,
    val name: String,
    val phoneNumber: String,
    val fcmToken: String? = null
)