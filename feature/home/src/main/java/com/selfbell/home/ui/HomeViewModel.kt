package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 임시 기본 위치 (예: 서울 시청)
val DEFAULT_LAT_LNG = LatLng(37.5665, 126.9780)

@HiltViewModel
class HomeViewModel @Inject constructor(
    // 필요한 Repository 또는 UseCase 주입
    // private val locationRepository: LocationRepository,
    // private val addressRepository: AddressRepository,
    // private val markerRepository: MarkerRepository
) : ViewModel() {

    private val _userLatLng = MutableStateFlow(DEFAULT_LAT_LNG)
    val userLatLng: StateFlow<LatLng> = _userLatLng.asStateFlow()

    private val _userAddress = MutableStateFlow("현재 위치 로딩 중...")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _userProfileImg = MutableStateFlow(R.drawable.usre_profileimg_icon)
    val userProfileImg: StateFlow<Int> = _userProfileImg.asStateFlow()

    private val _userProfileName = MutableStateFlow("잠자는 고양이")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _criminalMarkers = MutableStateFlow<List<MapMarkerData>>(emptyList())
    val criminalMarkers: StateFlow<List<MapMarkerData>> = _criminalMarkers.asStateFlow()

    private val _safetyBellMarkers = MutableStateFlow<List<MapMarkerData>>(emptyList())
    val safetyBellMarkers: StateFlow<List<MapMarkerData>> = _safetyBellMarkers.asStateFlow()

    private val _searchedLatLng = MutableStateFlow<LatLng?>(null)
    val searchedLatLng: StateFlow<LatLng?> = _searchedLatLng.asStateFlow()

    // --- 새로운 상태 및 콜백을 위한 멤버 추가 ---
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // ... (기존 loadInitialData 내용 유지) ...
        _userLatLng.value = LatLng(37.5645, 126.9780)
        _userAddress.value = "서울특별시 중구 세종대로 110"
        loadDummyMarkers()
    }

    private fun loadDummyMarkers() {
        _criminalMarkers.value = listOf(
            MapMarkerData(LatLng(37.5650, 126.9760), "범죄 발생 지역 A", MapMarkerData.MarkerType.CRIMINAL, "250m"),
            MapMarkerData(LatLng(37.5680, 126.9790), "범죄 발생 지역 B", MapMarkerData.MarkerType.CRIMINAL, "350m")
        )
        _safetyBellMarkers.value = listOf(
            MapMarkerData(LatLng(37.5655, 126.9770), "안심벨 1", MapMarkerData.MarkerType.SAFETY_BELL, "150m"),
            MapMarkerData(LatLng(37.5675, 126.9785), "안심벨 2", MapMarkerData.MarkerType.SAFETY_BELL, "50m")
        )
    }

    fun updateUserLocation(latLng: LatLng, address: String) {
        _userLatLng.value = latLng
        _userAddress.value = address
        // loadMarkersNearby(latLng)
    }

    // --- 검색 관련 함수들 ---
    fun onSearchTextChanged(newText: String) {
        _searchText.value = newText
        // 필요에 따라, 텍스트 변경 시 실시간 검색 결과 로드 로직 추가
        // 예: if (newText.length > 2) { searchAddressRealtime(newText) }
    }

    fun onSearchConfirmed() {
        val query = _searchText.value.trim()
        if (query.isNotBlank()) {
            // 기존 주소 검색 로직 호출
            searchAddress(query)
        } else {
            // 검색어가 비어있을 경우의 처리 (예: 알림 표시 또는 _searchedLatLng 초기화)
            // _searchedLatLng.value = null // 검색창이 비었을 때 지도 포커스를 현재 위치로 돌리고 싶다면
        }
    }

    // 기존 onAddressSearch를 내부 검색 로직 함수로 변경 (이름 변경 또는 private으로)
    private fun searchAddress(query: String) {
        viewModelScope.launch {
            // 실제 주소 검색 로직 (예: Geocoding API 호출)
            // try {
            //     val resultLatLng = addressRepository.getLatLngFromAddress(query)
            //     _searchedLatLng.value = resultLatLng
            //     if (resultLatLng == null) {
            //         // 검색 결과 없음 처리 (예: 사용자에게 알림)
            //     }
            // } catch (e: Exception) {
            //     // 오류 처리
            //     _searchedLatLng.value = null // 오류 발생 시
            // }

            println("ViewModel: Address search triggered for query: $query")
            // --- 임시 로직 시작 ---
            // 실제 구현에서는 API 호출 결과를 사용해야 합니다.
            // 여기서는 임의로 더미 마커 중 하나의 위치로 이동하거나, 사용자 현재 위치로 설정합니다.
            val foundMarker = (_criminalMarkers.value + _safetyBellMarkers.value)
                .find { it.address.contains(query, ignoreCase = true) }

            if (foundMarker != null) {
                _searchedLatLng.value = foundMarker.latLng
            } else {
                _searchedLatLng.value = _userLatLng.value // 검색 결과 없으면 사용자 위치로 (임시)
                // 또는 _searchedLatLng.value = null; 사용자에게 알림
            }
            // --- 임시 로직 끝 ---

            // 검색 후 _searchText를 비울 필요는 없음. 사용자가 검색어를 유지하고 싶어할 수 있음.
            // 검색 후 _searchedLatLng를 null로 바로 바꾸면 카메라 이동이 일어나지 않을 수 있음.
            // 카메라 이동 애니메이션이 완료된 후 HomeScreen에서 콜백을 통해 null로 설정하거나,
            // 일정 시간 뒤에 null로 설정하는 방식을 고려할 수 있습니다.
            // 혹은, searchedLatLng의 변경을 1회성 이벤트로 처리하는 Event Wrapper 클래스 사용도 고려.
        }
    }

    // --- 모달 아이템 클릭 관련 함수 ---
    fun onMapMarkerClicked(markerData: MapMarkerData) {
        // 클릭된 마커의 위치로 지도를 이동시키기 위해 _searchedLatLng 업데이트
        _searchedLatLng.value = markerData.latLng
        // 검색창 텍스트를 클릭된 마커의 주소로 업데이트 할 수도 있음 (선택 사항)
        // _searchText.value = markerData.address
        println("ViewModel: Modal marker item clicked - Address: ${markerData.address}, LatLng: ${markerData.latLng}")
        // 필요하다면 다른 로직 추가 (예: 상세 정보 화면으로 이동 준비)
    }

    // --- 메시지 신고 버튼 클릭 관련 함수 ---
    fun onReportMessageClicked() {
        println("ViewModel: Message report button clicked.")
        // TODO: 실제 메시지 신고 관련 로직 구현
        // 예: 다이얼로그 표시, 서버에 신고 데이터 전송 등
    }

    // ViewModel이 소멸될 때 _searchedLatLng를 null로 초기화하여
    // 앱을 다시 시작했을 때 이전 검색 위치로 카메라가 이동하는 것을 방지할 수 있습니다. (선택적)
    // override fun onCleared() {
    //     super.onCleared()
    //     _searchedLatLng.value = null
    // }
}

