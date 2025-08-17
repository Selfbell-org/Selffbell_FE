package com.selfbell.alerts.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.alerts.model.AlertData
import com.selfbell.alerts.model.AlertType
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    // 현재 선택된 알림 유형 (필터)
    private val _selectedAlertType = MutableStateFlow(AlertType.EMERGENCY_CALL)
    val selectedAlertType: StateFlow<AlertType> = _selectedAlertType

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _addressSearchQuery = MutableStateFlow("") // 주소 검색창 전용
    val addressSearchQuery: StateFlow<String> = _addressSearchQuery.asStateFlow()

    private val _searchedAddresses = MutableStateFlow<List<AddressModel>>(emptyList())
    val searchedAddresses: StateFlow<List<AddressModel>> = _searchedAddresses.asStateFlow()

    // 검색된 위치로 카메라를 이동시키기 위한 StateFlow
    private val _searchedLocationCameraTarget = MutableStateFlow<LatLng?>(null)
    val searchedLocationCameraTarget: StateFlow<LatLng?> = _searchedLocationCameraTarget.asStateFlow()

    // 주소 검색 결과 또는 오류 메시지
    private val _searchResultMessage = MutableStateFlow<String?>(null)
    val searchResultMessage: StateFlow<String?> = _searchResultMessage.asStateFlow()

    // 로딩 상태 (주소 검색 시)
    private val _isSearchingAddress = MutableStateFlow(false)
    val isSearchingAddress: StateFlow<Boolean> = _isSearchingAddress.asStateFlow()

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

    fun setAlertType(type: AlertType) {
        _selectedAlertType.value = type
        // 타입 변경 시 주소 검색 관련 상태 초기화 (선택적)
        _addressSearchQuery.value = ""
        _searchedAddresses.value = emptyList()
        _searchResultMessage.value = null
        // _searchedLocationCameraTarget.value = null // 카메라 타겟은 유지하거나 초기화
    }

    /**
     * AlertsModal의 검색창 텍스트 변경 시 호출 (알림 리스트 필터링용)
     */
    fun onAlertSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * AlertsModal의 검색창 텍스트 변경 시 호출 (주소 검색 API용)
     */
    fun onAddressSearchQueryChanged(query: String) {
        _addressSearchQuery.value = query
        if (query.isBlank()) {
            _searchedAddresses.value = emptyList()
            _searchResultMessage.value = null
            _searchedLocationCameraTarget.value = null // 검색창 비우면 카메라도 초기화 (선택적)
        }
    }

    /**
     * 주소 검색 실행 (네이버 Geocoding API 호출)
     */
    fun searchAddress() {
        val query = _addressSearchQuery.value.trim()
        if (query.isBlank()) {
            _searchResultMessage.value = "검색어를 입력해주세요."
            _searchedAddresses.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearchingAddress.value = true
            _searchResultMessage.value = null // 이전 메시지 초기화
            _searchedAddresses.value = emptyList() // 이전 결과 초기화
            try {
                val addresses = addressRepository.searchAddress(query)
                if (addresses.isNotEmpty()) {
                    _searchedAddresses.value = addresses
                    // 첫 번째 결과로 카메라 이동은 UI에서 선택 시 하거나, 여기서 바로 설정 가능
                    // val firstAddress = addresses[0]
                    // val lat = firstAddress.y.toDoubleOrNull()
                    // val lng = firstAddress.x.toDoubleOrNull()
                    // if (lat != null && lng != null) {
                    //     _searchedLocationCameraTarget.value = LatLng(lat, lng)
                    // }
                    _searchResultMessage.value = "${addresses.size}개의 주소 검색 결과"
                } else {
                    _searchResultMessage.value = "검색 결과가 없습니다."
                }
            } catch (e: Exception) {
                _searchResultMessage.value = "주소 검색 중 오류 발생: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isSearchingAddress.value = false
            }
        }
    }

    /**
     * 검색된 주소 항목 클릭 시 호출 (카메라 이동)
     */
    fun onSearchedAddressClicked(address: AddressModel) {
        val lat = address.y.toDoubleOrNull()
        val lng = address.x.toDoubleOrNull()
        Log.d("AlertAddress", "Address Clicked: ${address.roadAddress}, y=${address.y}, x=${address.x}, lat=$lat, lng=$lng") // 로그 추가
        if (lat != null && lng != null) {
            _searchedLocationCameraTarget.value = LatLng(lat, lng)
            Log.d("AlertAddress", "CameraTarget updated to: ${LatLng(lat, lng)}") // 로그 추가
            _searchResultMessage.value = "선택한 위치: ${address.roadAddress.ifEmpty { address.jibunAddress }}"
        } else {
            _searchResultMessage.value = "선택한 주소의 좌표 정보가 유효하지 않습니다."
            Log.e("AlertAddress", "Invalid coordinates for address: $address") // 로그 추가
        }
    }

    /**
     * 검색 결과 메시지가 소비된 후 초기화 (UI에서 호출, 예: SnackBar 표시 후)
     */
    fun onSearchResultMessageConsumed() {
        _searchResultMessage.value = null
    }
}