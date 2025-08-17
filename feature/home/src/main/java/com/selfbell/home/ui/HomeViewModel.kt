package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.ifEmpty
import kotlin.text.toDoubleOrNull

// 임시 기본 위치 (예: 서울 시청)
val DEFAULT_LAT_LNG = LatLng(37.5665, 126.9780)

@HiltViewModel
class HomeViewModel @Inject constructor(
    // 필요한 Repository 또는 UseCase 주입
    // private val locationRepository: LocationRepository,
     private val addressRepository: AddressRepository
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

    private val _cameraTargetLatLng = MutableStateFlow<LatLng?>(null)
    val cameraTargetLatLng: StateFlow<LatLng?> = _cameraTargetLatLng.asStateFlow()

    // 검색된 위치의 마커를 위한 StateFlow 추가
    private val _searchedLocationMarker = MutableStateFlow<MapMarkerData?>(null)
    val searchedLocationMarker: StateFlow<MapMarkerData?> = _searchedLocationMarker.asStateFlow()


    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // 검색 결과가 없을 때나 오류 발생 시 사용자에게 알릴 메시지
    private val _searchResultMessage = MutableStateFlow<String?>(null)
    val searchResultMessage: StateFlow<String?> = _searchResultMessage.asStateFlow()


    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _userLatLng.value = DEFAULT_LAT_LNG
        _userAddress.value = "서울특별시 중구 세종대로 110"
        _cameraTargetLatLng.value = _userLatLng.value
        loadDummyMarkers()
    }

    private fun loadDummyMarkers() {
        _criminalMarkers.value = listOf(
            MapMarkerData(LatLng(37.5650, 126.9760), "범죄 발생 지역 A", MapMarkerData.MarkerType.CRIMINAL, "250m"),
            MapMarkerData(LatLng(37.5680, 126.9790), "범죄 발생 지역 B", MapMarkerData.MarkerType.CRIMINAL, "350m"),
            MapMarkerData(LatLng(37.5750, 126.9950), "범죄 발생 지역 C", MapMarkerData.MarkerType.CRIMINAL, "1.2km"),
            MapMarkerData(LatLng(37.5500, 126.9700), "범죄 발생 지역 D", MapMarkerData.MarkerType.CRIMINAL, "1.8km"),
            MapMarkerData(LatLng(37.5610, 126.9850), "범죄 발생 지역 E", MapMarkerData.MarkerType.CRIMINAL, "700m"),
            MapMarkerData(LatLng(37.5700, 127.0000), "범죄 발생 지역 F", MapMarkerData.MarkerType.CRIMINAL, "1.5km")
        )
        _safetyBellMarkers.value = listOf(
            MapMarkerData(LatLng(37.5655, 126.9770), "안심벨 1", MapMarkerData.MarkerType.SAFETY_BELL, "150m"),
            MapMarkerData(LatLng(37.5675, 126.9785), "안심벨 2", MapMarkerData.MarkerType.SAFETY_BELL, "50m"),
            MapMarkerData(LatLng(37.5800, 126.9800), "안심벨 3", MapMarkerData.MarkerType.SAFETY_BELL, "2.0km"),
            MapMarkerData(LatLng(37.5600, 126.9900), "안심벨 4", MapMarkerData.MarkerType.SAFETY_BELL, "1.1km"),
            MapMarkerData(LatLng(37.5720, 126.9750), "안심벨 5", MapMarkerData.MarkerType.SAFETY_BELL, "900m"),
            MapMarkerData(LatLng(33.2500, 126.5600), "제주도 안심벨", MapMarkerData.MarkerType.SAFETY_BELL, "제주") // 제주도 위치
        )
    }
    fun updateUserLocation(latLng: LatLng, address: String) {
        _userLatLng.value = latLng
        _userAddress.value = address
        if (_searchedLocationMarker.value == null) {
            _cameraTargetLatLng.value = latLng
        }
        // loadMarkersNearby(latLng)
    }

    // --- 검색 관련 함수들 ---
    fun onSearchTextChanged(newText: String) {
        _searchText.value = newText
        if (newText.isBlank()) {
            _searchedLocationMarker.value = null
            _searchResultMessage.value = null
            // 검색창이 비었을 때 카메라를 현재 사용자 위치로 돌릴 수 있음
            // _cameraTargetLatLng.value = _userLatLng.value
        }
    }

    fun onSearchConfirmed() {
        val query = _searchText.value.trim()
        if (query.isNotBlank()) {
            searchAddress(query)
        } else {
            _searchedLocationMarker.value = null
            _searchResultMessage.value = "검색어를 입력해주세요."
            // 검색어가 비었을 때 카메라를 현재 사용자 위치로 돌리고 싶다면
            _cameraTargetLatLng.value = _userLatLng.value
        }
    }

    // 기존 onAddressSearch를 내부 검색 로직 함수로 변경 (이름 변경 또는 private으로)
    private fun searchAddress(query: String) {
        viewModelScope.launch {
            try {
                // 이전에 표시된 검색 결과 마커 및 메시지 초기화
                _searchedLocationMarker.value = null
                _searchResultMessage.value = null

                val addresses: List<AddressModel> = addressRepository.searchAddress(query)

                if (addresses.isNotEmpty()) {
                    // 첫 번째 검색 결과를 사용
                    val firstAddress = addresses[0]
                    val lat = firstAddress.y.toDoubleOrNull() // y 좌표가 위도(latitude)
                    val lng = firstAddress.x.toDoubleOrNull() // x 좌표가 경도(longitude)

                    if (lat != null && lng != null) {
                        val searchedLatLng = LatLng(lat, lng)
                        // 검색된 위치에 표시할 마커 데이터 생성
                        // MapMarkerData.MarkerType에 SEARCH_RESULT와 같은 타입을 추가하거나 기존 타입 활용
                        // 여기서는 임시로 CRIMINAL 타입을 사용하고, 주소를 title로 사용
                        _searchedLocationMarker.value = MapMarkerData(
                            latLng = searchedLatLng,
                            // title: 도로명 주소가 있으면 사용, 없으면 지번 주소 사용
                            address = firstAddress.roadAddress.ifEmpty { firstAddress.jibunAddress },
                            type = MapMarkerData.MarkerType.USER, // TODO: 검색 결과용 마커 타입으로 변경
                            distance = "" // 거리 계산 함수 사용
                        )
                        // 검색된 위치로 카메라 이동
                        _cameraTargetLatLng.value = searchedLatLng
                        _searchResultMessage.value = "검색 결과: ${firstAddress.roadAddress.ifEmpty { firstAddress.jibunAddress }}"
                    } else {
                        // 좌표 변환 실패 처리
                        _searchResultMessage.value = "주소의 좌표 정보를 가져올 수 없습니다."
                        _cameraTargetLatLng.value = _userLatLng.value // 기본 위치로 카메라 이동
                    }
                } else {
                    // 검색 결과가 없는 경우
                    _searchResultMessage.value = "검색 결과가 없습니다. 다른 검색어를 시도해보세요."
                    _cameraTargetLatLng.value = _userLatLng.value // 기본 위치로 카메라 이동
                }
            } catch (e: Exception) {
                // API 호출 중 오류 발생 처리 (네트워크 오류 등)
                _searchResultMessage.value = "주소 검색 중 오류가 발생했습니다: ${e.message}"
                _cameraTargetLatLng.value = _userLatLng.value // 기본 위치로 카메라 이동
                // 예외 로깅
                e.printStackTrace()
            }
        }
    }

    // --- 모달 아이템 클릭 관련 함수 ---
    fun onMapMarkerClicked(markerData: MapMarkerData) {
        // 클릭된 마커의 위치로 지도를 이동시키기 위해 _searchedLatLng 업데이트
        _cameraTargetLatLng.value = markerData.latLng
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

