package com.selfbell.escort.ui

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EscortViewModel @Inject constructor() : ViewModel() {
    // TODO: 실제로는 유저의 현재 위치와 메인 주소를 Repository/UseCase를 통해 불러와야 합니다.

    // 출발지 상태
    private val _startLocation = MutableStateFlow(LocationState("현재 위치", LatLng(37.5665, 126.9780)))
    val startLocation = _startLocation.asStateFlow()

    // 도착지 상태
    private val _destinationLocation = MutableStateFlow(LocationState("메인 주소", LatLng(37.5665, 126.9780)))
    val destinationLocation = _destinationLocation.asStateFlow()

    // 도착 시간 모드 (타이머 vs 도착 예정 시간)
    private val _arrivalMode = MutableStateFlow(ArrivalMode.TIMER)
    val arrivalMode = _arrivalMode.asStateFlow()

    // 타이머 시간 (분)
    private val _timerMinutes = MutableStateFlow(30)
    val timerMinutes = _timerMinutes.asStateFlow()

    // 출발지 주소 업데이트 함수
    fun updateStartLocation(name: String, latLng: LatLng) {
        _startLocation.value = LocationState(name, latLng)
    }

    // 도착지 주소 업데이트 함수
    fun updateDestinationLocation(name: String, latLng: LatLng) {
        _destinationLocation.value = LocationState(name, latLng)
    }

    // 도착 모드 변경 함수
    fun setArrivalMode(mode: ArrivalMode) {
        _arrivalMode.value = mode
    }

    // 타이머 시간 업데이트 함수
    fun setTimerMinutes(minutes: Int) {
        _timerMinutes.value = minutes
    }
}

data class LocationState(
    val name: String,
    val latLng: LatLng
)

enum class ArrivalMode {
    TIMER,
    SCHEDULED_TIME
}