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