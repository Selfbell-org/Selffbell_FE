package com.selfbell.domain.model

import java.time.LocalDateTime

data class SafeWalkHistoryItem(
    val id: Long,
    val userProfileUrl: String?, // 사용자 프로필 이미지 URL
    val userName: String,       // "나의 귀가" 또는 상대방 이름
    val userType: String,       // "나", "보호자", "피보호자" 등
    val destinationName: String, // 목적지 주소 또는 별칭 (예: "집")
    val dateTime: LocalDateTime,
    val status: SafeWalkStatus // "귀가중", "완료" 등
)

//enum class SafeWalkStatus {
//    IN_PROGRESS, // 귀가중
//    COMPLETED,   // 완료
//    CANCELED     // 취소됨
//}