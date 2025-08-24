package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.*
import com.selfbell.domain.repository.SafeWalkRepository
import com.selfbell.domain.repository.ReverseGeocodingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

    // ✅ [추가] 트랙 좌표 목록을 저장할 StateFlow
    private val _trackCoordinates = MutableStateFlow<List<LatLng>>(emptyList())
    val trackCoordinates: StateFlow<List<LatLng>> = _trackCoordinates.asStateFlow()

    fun loadSafeWalkDetail(sessionId: Long) {
        _uiState.value = HistoryDetailUiState.Loading
        _trackCoordinates.value = emptyList()

        viewModelScope.launch {
            try {
                // 👇 [수정] 상세 정보와 트랙 목록을 동시에 요청하여 더 빠르게 로드
                val detailDeferred = async { safeWalkRepository.getSafeWalkDetail(sessionId) }
                val tracksDeferred = async { safeWalkRepository.getTracks(sessionId, null, 500, "asc") }

                val detail = detailDeferred.await()
                val tracks = tracksDeferred.await()

                // 트랙 목록을 LatLng 리스트로 변환하여 상태 업데이트
                _trackCoordinates.value = tracks.map { LatLng(it.lat, it.lon) }
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
