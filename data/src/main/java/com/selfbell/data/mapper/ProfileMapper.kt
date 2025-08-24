package com.selfbell.data.mapper

// data/mapper/ProfileMapper.kt (예시)
import com.selfbell.data.api.response.ProfileResponseDto
import com.selfbell.domain.model.Profile

fun ProfileResponseDto.toProfile(): Profile {
    return Profile(
        name = this.name,
        phoneNumber = this.phoneNumber
    )
}