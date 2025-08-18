// domain/User.kt

package com.selfbell.domain

/**
 * 앱의 핵심 사용자 모델.
 */
data class User(
    val id: String,
    val phoneNumber: String,
    val name: String?,
    val profileImageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)