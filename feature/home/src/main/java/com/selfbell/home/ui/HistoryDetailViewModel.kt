package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import javax.inject.Inject

sealed interface HistoryDetailUiState {
    object Loading : HistoryDetailUiState
    data class Success(val detail: SafeWalkDetail) : HistoryDetailUiState
    data class Error(val message: String) : HistoryDetailUiState
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val safeWalkRepository: SafeWalkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    fun loadSafeWalkDetail(sessionId: Long) {
        _uiState.value = HistoryDetailUiState.Loading
        viewModelScope.launch {
            /*
            // --- ⬇️ 실제 API 연동 코드 (현재 주석 처리) ⬇️ ---
            try {
                val detail = safeWalkRepository.getSafeWalkDetail(sessionId)
                _uiState.value = HistoryDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = HistoryDetailUiState.Error(e.message ?: "상세 정보 로드 실패")
            }
            */

            // --- ⬇️ 더미데이터 테스트 코드 (현재 사용) ⬇️ ---
            delay(1000) // 로딩 효과를 위한 딜레이
            val dummyDetail = createDummyDetail(sessionId)
            _uiState.value = HistoryDetailUiState.Success(dummyDetail)
        }
    }
}

// 더미데이터 생성 함수
private fun createDummyDetail(sessionId: Long): SafeWalkDetail {
    val startTime = LocalDateTime.of(2025, 8, 15, 10, 40) // 2025년 8월 15일 오전 10시 40분
    val expectedTime = LocalDateTime.of(2025, 8, 15, 10, 45) // 10:45 예상 도착
    val actualTime = LocalDateTime.of(2025, 8, 15, 10, 40) // 10:40 실제 도착 (5분 전)
    
    return SafeWalkDetail(
        sessionId = sessionId,
        ward = Ward(id = 123, nickname = "아빠"),
        origin = LocationDetail(37.5665, 126.9780, "출발지"),
        destination = LocationDetail(37.4943, 126.9583, "서울특별시 동작구 현충로 119 효창공원앞"),
        status = SafeWalkStatus.COMPLETED,
        startedAt = startTime,
        expectedArrival = expectedTime,
        timerEnd = null,
        guardians = listOf(Guardian(id = 456, nickname = "엄마")),
        endedAt = actualTime
    )
}