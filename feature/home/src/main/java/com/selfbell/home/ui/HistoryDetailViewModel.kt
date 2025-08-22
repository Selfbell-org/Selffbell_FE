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
    return SafeWalkDetail(
        sessionId = sessionId,
        ward = Ward(id = 123, nickname = "나의 귀가"),
        origin = LocationDetail(37.4989, 126.9560, "출발지"),
        destination = LocationDetail(37.4943, 126.9583, "집"),
        status = SafeWalkStatus.COMPLETED,
        startedAt = LocalDateTime.now().minusHours(1),
        expectedArrival = LocalDateTime.now().minusMinutes(30),
        timerEnd = null,
        guardians = listOf(Guardian(id = 456, nickname = "엄마")),
        endedAt = LocalDateTime.now().minusMinutes(25)
    )
}