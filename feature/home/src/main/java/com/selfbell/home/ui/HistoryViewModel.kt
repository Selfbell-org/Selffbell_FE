package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.HistoryDateFilter
import com.selfbell.domain.model.HistoryFilter
import com.selfbell.domain.model.HistorySortOrder
import com.selfbell.domain.model.HistoryUserFilter
import com.selfbell.domain.model.SafeWalkHistoryItem
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ UI 상태를 정의하는 Sealed Interface
sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(
        val historyItems: List<SafeWalkHistoryItem>
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val safeWalkRepository: SafeWalkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // ✅ 현재 필터 상태를 저장하는 StateFlow
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
            try {
                val filter = currentFilter.value
                val history = safeWalkRepository.getSafeWalkHistory(filter)
                _uiState.value = HistoryUiState.Success(history)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "히스토리 로드 실패")
            }
        }
    }

    // ✅ 필터 변경 함수
    fun setFilter(newFilter: HistoryFilter) {
        currentFilter.value = newFilter
        loadHistory()
    }
}