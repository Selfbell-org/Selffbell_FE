package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val historyItems: List<SafeWalkHistoryItem>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val safeWalkRepository: SafeWalkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    val currentFilter = MutableStateFlow(
        HistoryFilter(
            userType = HistoryUserFilter.ALL,
            dateRange = HistoryDateFilter.WEEK,
            sortOrder = HistorySortOrder.LATEST
        )
    )

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            /* // TODO: API 연동 시 아래 주석을 해제하고, 더미데이터 코드를 삭제하세요.
            try {
                val filter = currentFilter.value
                val history = safeWalkRepository.getSafeWalkHistory(filter)
                _uiState.value = HistoryUiState.Success(history)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "히스토리 로드 실패")
            }
            */

            // --- 더미데이터 로직 ---
            delay(1000) // 로딩 효과를 위한 딜레이
            val dummyData = createDummyHistoryData()
            val filteredData = dummyData.filter { it.userType == currentFilter.value.userType.name || currentFilter.value.userType == HistoryUserFilter.ALL }
                .sortedBy { it.dateTime }
                .let { if (currentFilter.value.sortOrder == HistorySortOrder.LATEST) it.reversed() else it }
            _uiState.value = HistoryUiState.Success(filteredData)
            // --- 더미데이터 로직 끝 ---
        }
    }

    fun setFilter(newFilter: HistoryFilter) {
        currentFilter.value = newFilter
        loadHistory()
    }
}

// ✅ 더미데이터 생성 함수
private fun createDummyHistoryData(): List<SafeWalkHistoryItem> {
    return listOf(
        SafeWalkHistoryItem(
            id = 1,
            userProfileUrl = null,
            userName = "엄마",
            userType = "GUARDIAN",
            destinationName = "집",
            dateTime = LocalDateTime.now().minusDays(1),
            status = SafeWalkStatus.IN_PROGRESS
        ),
        SafeWalkHistoryItem(
            id = 2,
            userProfileUrl = null,
            userName = "나의 귀가",
            userType = "MINE",
            destinationName = "회사",
            dateTime = LocalDateTime.now().minusDays(2),
            status = SafeWalkStatus.COMPLETED
        ),
        SafeWalkHistoryItem(
            id = 3,
            userProfileUrl = null,
            userName = "친구",
            userType = "GUARDIAN",
            destinationName = "집",
            dateTime = LocalDateTime.now().minusDays(3),
            status = SafeWalkStatus.ENDED
        ),
        SafeWalkHistoryItem(
            id = 4,
            userProfileUrl = null,
            userName = "나의 귀가",
            userType = "MINE",
            destinationName = "학교",
            dateTime = LocalDateTime.now().minusDays(4),
            status = SafeWalkStatus.CANCELED
        ),
    )
}