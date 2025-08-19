package com.selfbell.domain.model

data class ContactRelationship(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val fromPhoneNumber: String,
    val toPhoneNumber: String,
    val status: ContactRelationshipStatus,
    val createdAt: String,
    val updatedAt: String
)