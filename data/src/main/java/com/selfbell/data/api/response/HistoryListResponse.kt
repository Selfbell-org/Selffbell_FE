package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName

// API 응답의 최상위 구조
data class HistoryListResponse(
    val sessions: List<HistorySessionDto>
)

// 개별 세션의 구조
data class HistorySessionDto(
    val ward: WardDto,
    val session: SessionDto
)

data class WardDto(
    val id: Long,
    val name: String
)

data class SessionDto(
    val id: Long,
    val status: String,
    @SerializedName("addressName")
    val destinationName: String,
    val startedAt: String
)