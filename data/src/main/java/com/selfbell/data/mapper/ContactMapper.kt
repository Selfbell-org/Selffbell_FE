package com.selfbell.data.mapper

import com.selfbell.data.api.response.ContactResponseDto
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactRelationshipStatus

fun ContactResponseDto.toDomain(): ContactRelationship {
    val statusEnum = when (status.uppercase()) {
        "PENDING" -> ContactRelationshipStatus.PENDING
        "ACCEPTED" -> ContactRelationshipStatus.ACCEPTED
        "REJECTED" -> ContactRelationshipStatus.REJECTED
        else -> ContactRelationshipStatus.NONE
    }
    return ContactRelationship(
        id = contactId.toString(),
        fromUserId = "", // not provided by API
        toUserId = "",   // not provided by API
        fromPhoneNumber = me.phoneNumber,
        toPhoneNumber = other.phoneNumber,
        status = statusEnum,
        createdAt = "",
        updatedAt = "",
        sharePermission = sharePermission
    )
} 