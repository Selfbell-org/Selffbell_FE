package com.selfbell.domain.model

data class ContactUser(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isExists: Boolean = false,
    val relationshipStatus: ContactRelationshipStatus = ContactRelationshipStatus.NONE
)

enum class ContactRelationshipStatus {
    NONE,           // 관계 없음
    PENDING,        // 요청 대기 중
    ACCEPTED,       // 수락됨
    REJECTED        // 거절됨
} 