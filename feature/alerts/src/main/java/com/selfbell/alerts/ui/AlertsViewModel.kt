// feature/alerts/ui/AlertsViewModel.kt
package com.selfbell.alerts.ui

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import com.selfbell.alerts.model.AlertData
import com.selfbell.alerts.model.AlertType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor() : ViewModel() {

    // 현재 선택된 알림 유형 (필터)
    private val _selectedAlertType = MutableStateFlow(AlertType.EMERGENCY_CALL)
    val selectedAlertType: StateFlow<AlertType> = _selectedAlertType

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    // 더미 알림 데이터 (실제로는 API 또는 DB에서 가져옴)
    private val _allAlerts = MutableStateFlow<List<AlertData>>(
        listOf(
            // 긴급신고 (10개)
            AlertData(1, LatLng(37.564, 126.974), "긴급신고-선유공원앞", 358, AlertType.EMERGENCY_CALL),
            AlertData(2, LatLng(37.568, 126.978), "긴급신고-교대역 2번출구 앞", 420, AlertType.EMERGENCY_CALL),
            AlertData(3, LatLng(37.562, 126.972), "긴급신고-영등포시장", 512, AlertType.EMERGENCY_CALL),
            AlertData(4, LatLng(37.569, 126.980), "긴급신고-시청역 인근", 680, AlertType.EMERGENCY_CALL),
            AlertData(5, LatLng(37.555, 126.965), "긴급신고-여의도공원", 850, AlertType.EMERGENCY_CALL),
            AlertData(6, LatLng(37.561, 126.975), "긴급신고-종로3가", 910, AlertType.EMERGENCY_CALL),
            AlertData(7, LatLng(37.575, 126.990), "긴급신고-동대문역사공원", 1020, AlertType.EMERGENCY_CALL),
            AlertData(8, LatLng(37.567, 126.970), "긴급신고-광화문광장", 1250, AlertType.EMERGENCY_CALL),
            AlertData(9, LatLng(37.558, 126.979), "긴급신고-명동역 인근", 1500, AlertType.EMERGENCY_CALL),
            AlertData(10, LatLng(37.550, 126.985), "긴급신고-남산타워 입구", 1800, AlertType.EMERGENCY_CALL),

            // 범죄자 위치정보 (10개)
            AlertData(11, LatLng(37.562, 126.972), "범죄자 위치정보", 421, AlertType.CRIMINAL_INFO),
            AlertData(12, LatLng(37.570, 126.982), "범죄자 위치정보", 654, AlertType.CRIMINAL_INFO),
            AlertData(13, LatLng(37.565, 126.985), "범죄자 위치정보", 789, AlertType.CRIMINAL_INFO),
            AlertData(14, LatLng(37.559, 126.971), "범죄자 위치정보", 955, AlertType.CRIMINAL_INFO),
            AlertData(15, LatLng(37.572, 126.995), "범죄자 위치정보", 1123, AlertType.CRIMINAL_INFO),
            AlertData(16, LatLng(37.545, 126.960), "범죄자 위치정보", 1345, AlertType.CRIMINAL_INFO),
            AlertData(17, LatLng(37.569, 126.999), "범죄자 위치정보", 1450, AlertType.CRIMINAL_INFO),
            AlertData(18, LatLng(37.551, 126.975), "범죄자 위치정보", 1680, AlertType.CRIMINAL_INFO),
            AlertData(19, LatLng(37.578, 127.005), "범죄자 위치정보", 1920, AlertType.CRIMINAL_INFO),
            AlertData(20, LatLng(37.563, 126.988), "범죄자 위치정보", 2100, AlertType.CRIMINAL_INFO)
        ).sortedBy { it.distance }
    )
    val allAlerts: StateFlow<List<AlertData>> = _allAlerts

    // 알림 유형 필터 변경
    fun setAlertType(type: AlertType) {
        _selectedAlertType.value = type
    }
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}