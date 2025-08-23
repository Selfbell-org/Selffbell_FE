package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import com.selfbell.domain.repository.ReverseGeocodingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime

sealed interface HistoryDetailUiState {
    object Loading : HistoryDetailUiState
    data class Success(val detail: SafeWalkDetail) : HistoryDetailUiState
    data class Error(val message: String) : HistoryDetailUiState
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val safeWalkRepository: SafeWalkRepository,
    private val reverseGeocodingRepository: ReverseGeocodingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    fun loadSafeWalkDetail(sessionId: Long) {
        _uiState.value = HistoryDetailUiState.Loading
        viewModelScope.launch {
            try {
                val detail = safeWalkRepository.getSafeWalkDetail(sessionId)
                _uiState.value = HistoryDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = HistoryDetailUiState.Error(e.message ?: "상세 정보 로드 실패")
            }
        }
    }
    
    /**
     * 위도/경도 좌표를 주소로 변환합니다.
     * 
     * @param lat 위도
     * @param lon 경도
     * @return 주소 문자열, 실패 시 null
     */
    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): String? {
        return try {
            reverseGeocodingRepository.reverseGeocode(lat, lon)
        } catch (e: Exception) {
            null
        }
    }
}

// 더미데이터 생성 함수
private fun createDummyDetail(sessionId: Long): SafeWalkDetail {
    val startTime = LocalDateTime.of(2025, 8, 15, 10, 40) // 2025년 8월 15일 오전 10시 40분
    val expectedStartTime = LocalDateTime.of(2025, 8, 15, 10, 25) // 상대가 설정한 시작 시간 10:25
    val expectedEndTime = LocalDateTime.of(2025, 8, 15, 10, 45)   // 상대가 설정한 종료 시간 10:45
    val actualTime = LocalDateTime.of(2025, 8, 15, 10, 40) // 10:40 실제 도착 (5분 전)
    
    return SafeWalkDetail(
        sessionId = sessionId,
        ward = Ward(id = 123, nickname = "아빠"),
        origin = LocationDetail(37.5665, 126.9780, "출발지"),
        destination = LocationDetail(37.4943, 126.9583, "서울특별시 동작구 현충로 119 효창공원앞"),
        status = SafeWalkStatus.COMPLETED,
        startedAt = startTime,
        expectedArrival = expectedEndTime,
        timerEnd = null,
        guardians = listOf(Guardian(id = 456, nickname = "엄마")),
        endedAt = actualTime,
        // ✅ 서버에서 받아올 추가 데이터들
        expectedStartTime = expectedStartTime,
        expectedEndTime = expectedEndTime,
        estimatedDurationMinutes = 20, // 10:25 ~ 10:45 (20분)
        actualDurationMinutes = 15,    // 실제 소요 시간
        timeDifferenceMinutes = -5     // 예상보다 5분 빨리 도착
    )
}