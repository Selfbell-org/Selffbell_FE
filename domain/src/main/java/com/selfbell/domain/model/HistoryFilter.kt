package com.selfbell.domain.model

data class HistoryFilter(
    val userType: HistoryUserFilter,
    val dateRange: HistoryDateFilter,
    val sortOrder: HistorySortOrder
)

enum class HistoryUserFilter {
    ALL,        // 전체 기록
    GUARDIANS,  // 보호자/피보호자
    MINE        // 나의 귀가
}

enum class HistoryDateFilter {
    WEEK,       // 최근 1주일
    MONTH,      // 최근 30일
    YEAR,       // 최근 1년
    ALL         // 전체
}

enum class HistorySortOrder {
    LATEST,     // 최신순
    OLDEST      // 오래된순
}