package com.selfbell.data.api

import com.selfbell.data.api.response.SafeWalkHistoryResponse
import com.selfbell.domain.model.HistoryDateFilter
import com.selfbell.domain.model.HistorySortOrder
import com.selfbell.domain.model.HistoryUserFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeWalkService @Inject constructor(
    private val safeWalksApi: SafeWalksApi
) {
    suspend fun getSafeWalkHistory(
        userType: HistoryUserFilter,
        dateRange: HistoryDateFilter,
        sortOrder: HistorySortOrder
    ): SafeWalkHistoryResponse {
        val response = safeWalksApi.getSafeWalkHistory(
            userType = userType.name,
            dateRange = dateRange.name,
            sortOrder = sortOrder.name
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("응답 본문이 비어있습니다.")
        } else {
            throw Exception("API 호출 실패: ${response.code()}")
        }
    }
}