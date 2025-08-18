package com.selfbell.home.ui
// HomeViewModel.kt (수정된 전체 코드)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.HomeRepository
import com.selfbell.domain.User
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.repository.EmergencyBellRepository
import com.selfbell.home.model.MapMarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.ifEmpty
import kotlin.text.toDoubleOrNull

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val userProfile: User,
        val userLatLng: LatLng,
        val criminalMarkers: List<MapMarkerData>,
        val safetyBellMarkers: List<MapMarkerData>,
        val emergencyBells: List<EmergencyBell>,
        val selectedEmergencyBellDetail: EmergencyBellDetail? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

val DEFAULT_LAT_LNG = LatLng(37.5665, 126.9780)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val addressRepository: AddressRepository,
    private val emergencyBellRepository: EmergencyBellRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _cameraTargetLatLng = MutableStateFlow<LatLng?>(null)
    val cameraTargetLatLng: StateFlow<LatLng?> = _cameraTargetLatLng.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResultMessage = MutableStateFlow<String?>(null)
    val searchResultMessage: StateFlow<String?> = _searchResultMessage.asStateFlow()

    init {
        loadHomeScreenData()
    }

    private fun loadHomeScreenData() {
        viewModelScope.launch {
            try {
                // homeRepository를 통해 사용자 프로필을 가져옴
                val userProfile: User = homeRepository.getUserProfile()

                val criminalMarkers = loadDummyCriminalMarkers()
                val safetyBellMarkers = loadDummySafetyBellMarkers()

                // ✅ userProfile에서 위도, 경도 정보 추출
                val userLatLng = LatLng(userProfile.latitude, userProfile.longitude)
                val emergencyBells = emergencyBellRepository.getNearbyEmergencyBells( // ✅ API 호출
                    lat = userLatLng.latitude,
                    lon = userLatLng.longitude,
                    radius = 1000 // 1000m 반경 설정
                )

                _uiState.value = HomeUiState.Success(
                    userProfile = userProfile,
                    userLatLng = userLatLng, // ✅ userLatLng 전달
                    criminalMarkers = criminalMarkers,
                    safetyBellMarkers = safetyBellMarkers,
                    emergencyBells = emergencyBells
                )
                _cameraTargetLatLng.value = userLatLng
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "데이터 로딩 실패")
            }
        }
    }

    // ✅ UI 상태를 변경하는 함수 추가
    fun setSelectedEmergencyBellDetail(detail: EmergencyBellDetail?) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedEmergencyBellDetail = detail)
        }
    }

    // ✅ 안심벨 상세 정보를 가져오는 함수 추가
    fun getEmergencyBellDetail(objtId: Int) {
        viewModelScope.launch {
            try {
                val detail = emergencyBellRepository.getEmergencyBellDetail(objtId)
                setSelectedEmergencyBellDetail(detail)
            } catch (e: Exception) {
                // TODO: 상세 정보 가져오기 실패 처리
                setSelectedEmergencyBellDetail(null)
            }
        }
    }

    private fun loadDummyCriminalMarkers(): List<MapMarkerData> {
        return listOf(
            MapMarkerData(LatLng(37.5650, 126.9760), "범죄 발생 지역 A", MapMarkerData.MarkerType.CRIMINAL, 250.0),
            MapMarkerData(LatLng(37.5680, 126.9790), "범죄 발생 지역 B", MapMarkerData.MarkerType.CRIMINAL, 300.0)
        )
    }

    private fun loadDummySafetyBellMarkers(): List<MapMarkerData> {
        return listOf(
            MapMarkerData(LatLng(37.5655, 126.9770), "안심벨 1", MapMarkerData.MarkerType.SAFETY_BELL, 150.0),
            MapMarkerData(LatLng(37.5675, 126.9785), "안심벨 2", MapMarkerData.MarkerType.SAFETY_BELL, 50.0)
        )
    }

    fun onSearchTextChanged(newText: String) {
        _searchText.value = newText
    }

    fun onSearchConfirmed() {
        val query = _searchText.value.trim()
        if (query.isNotBlank()) {
            searchAddress(query)
        } else {
            _searchResultMessage.value = "검색어를 입력해주세요."
            _cameraTargetLatLng.value = null
        }
    }

    private fun searchAddress(query: String) {
        viewModelScope.launch {
            try {
                val addresses: List<AddressModel> = addressRepository.searchAddress(query)

                if (addresses.isNotEmpty()) {
                    val firstAddress = addresses[0]
                    val lat = firstAddress.y.toDoubleOrNull()
                    val lng = firstAddress.x.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        _cameraTargetLatLng.value = LatLng(lat, lng)
                        _searchResultMessage.value = "검색 결과: ${firstAddress.roadAddress.ifEmpty { firstAddress.jibunAddress }}"
                    } else {
                        _searchResultMessage.value = "주소의 좌표 정보를 가져올 수 없습니다."
                        _cameraTargetLatLng.value = null
                    }
                } else {
                    _searchResultMessage.value = "검색 결과가 없습니다. 다른 검색어를 시도해보세요."
                    _cameraTargetLatLng.value = null
                }
            } catch (e: Exception) {
                _searchResultMessage.value = "주소 검색 중 오류가 발생했습니다: ${e.message}"
                _cameraTargetLatLng.value = null
            }
        }
    }

    fun onMapMarkerClicked(markerData: MapMarkerData) {
        _cameraTargetLatLng.value = markerData.latLng
        _searchText.value = markerData.address
        _searchResultMessage.value = null
    }
}