package com.selfbell.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.ContactRepository
import com.selfbell.domain.model.Criminal
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.repository.CriminalRepository
import com.selfbell.domain.repository.EmergencyBellRepository
import com.selfbell.data.repository.impl.TokenManager
import com.selfbell.home.model.MapMarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.selfbell.core.location.LocationTracker
import kotlin.text.ifEmpty
import kotlin.text.toDoubleOrNull
import com.selfbell.core.model.Contact // ✅ Contact 모델 import

// ✅ 지도의 마커 표시 모드를 위한 enum 클래스 (HomeViewModel이 접근 가능하도록 이 파일에 추가)
enum class MapMarkerMode {
    SAFETY_BELL_ONLY,
    SAFETY_BELL_AND_CRIMINALS
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val userLatLng: LatLng,
        val emergencyBells: List<EmergencyBell>,
        val criminals: List<Criminal>,
        val selectedEmergencyBellDetail: EmergencyBellDetail? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

val DEFAULT_LAT_LNG = LatLng(37.5665, 126.9780)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val emergencyBellRepository: EmergencyBellRepository,
    private val criminalRepository: CriminalRepository,
    private val locationTracker: LocationTracker,
    private val tokenManager: TokenManager,
    private val emergencyRepository: EmergencyBellRepository, // ✅ EmergencyRepository 주입
    private val contactRepository: ContactRepository // ✅ ContactRepository 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _cameraTargetLatLng = MutableStateFlow<LatLng?>(null)
    val cameraTargetLatLng: StateFlow<LatLng?> = _cameraTargetLatLng.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResultMessage = MutableStateFlow<String?>(null)
    val searchResultMessage: StateFlow<String?> = _searchResultMessage.asStateFlow()

    // ✅ 지도의 마커 모드를 관리하는 상태 (StateFlow)
    private val _mapMarkerMode = MutableStateFlow(MapMarkerMode.SAFETY_BELL_ONLY)
    val mapMarkerMode: StateFlow<MapMarkerMode> = _mapMarkerMode.asStateFlow()

    // ✅ 범죄자 정보를 미리 로드하여 저장할 별도의 StateFlow
    private val _preloadedCriminals = MutableStateFlow<List<Criminal>>(emptyList())
    // UI에서 접근할 수 있도록 공개된 StateFlow 추가
    val criminals: StateFlow<List<Criminal>> = _preloadedCriminals.asStateFlow()

    // ✅ 범죄자 데이터 로딩 상태를 위한 StateFlow 추가
    private val _isCriminalsLoading = MutableStateFlow(false)
    val isCriminalsLoading: StateFlow<Boolean> = _isCriminalsLoading.asStateFlow()

    // ✅ 긴급 신고 시 선택된 보호자 목록 (더미 데이터)
    private val _selectedGuardians = MutableStateFlow(
        listOf(
            Contact(1L, null, "엄마", "01011112222", "fcm_token_1"),
            Contact(2L, null, "아빠", "01033334444", "fcm_token_2"),
        )
    )
    val selectedGuardians: StateFlow<List<Contact>> = _selectedGuardians.asStateFlow()

    // ✅ 긴급 신고 메시지 템플릿
    private val _messageTemplates = MutableStateFlow(
        listOf(
            "위급 상황입니다. 제 위치를 확인해주세요.",
            "현재 위험에 처해있습니다. 도움을 요청합니다."
        )
    )
    val messageTemplates: StateFlow<List<String>> = _messageTemplates.asStateFlow()


    init {
        startHomeLocationStream()
        loadAcceptedGuardians()
        // ✅ 추가: _preloadedCriminals의 변경을 감지하여 UIState를 업데이트합니다.
        viewModelScope.launch {
            _preloadedCriminals.collectLatest { criminalsList ->
                val current = _uiState.value
                if (current is HomeUiState.Success) {
                    // 범죄자 리스트가 업데이트되면 기존 UIState의 criminals를 새 데이터로 교체
                    _uiState.value = current.copy(criminals = criminalsList)
                }
            }
        }
    }

    private fun startHomeLocationStream() {
        viewModelScope.launch {
            try {
                locationTracker.getLocationUpdates().collectLatest { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    // 안전벨 정보만 가져오기
                    val emergencyBells = emergencyBellRepository.getNearbyEmergencyBells(
                        lat = userLatLng.latitude,
                        lon = userLatLng.longitude,
                        radius = 500
                    ).sortedBy { it.distance ?: Double.MAX_VALUE }

                    val current = _uiState.value
                    if (current !is HomeUiState.Success) {
                        // 초기 로딩 시에만 범죄자 정보 미리 로드 시작
                        fetchCriminals(userLatLng)
                    }

                    // UI 상태 업데이트
                    _uiState.value = HomeUiState.Success(
                        userLatLng = userLatLng,
                        emergencyBells = emergencyBells,
                        criminals = _preloadedCriminals.value
                    )

                    if (_cameraTargetLatLng.value == null) {
                        _cameraTargetLatLng.value = userLatLng
                    }

                    Log.d("HomeViewModel", "안전벨 ${emergencyBells.size}개 로드 완료")
                    emergencyBells.take(3).forEach { bell ->
                        Log.d("HomeViewModel", "안전벨: ${bell.detail}, 거리: ${bell.distance?.let { "${it.toInt()}m" } ?: "알 수 없음"}")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "위치 스트림 처리 실패", e)
                _uiState.value = HomeUiState.Error(e.message ?: "데이터 로딩 실패")
            }
        }
    }

    private fun loadAcceptedGuardians() {
        viewModelScope.launch {
            try {
                if (tokenManager.hasValidToken()) {
                    val relationships = contactRepository.getContactsFromServer(status = "ACCEPTED", page = 0, size = 100)
                    val guardians = mutableListOf<Contact>()
                    
                    for (rel in relationships) {
                        val phone = if (rel.toPhoneNumber.isNotBlank()) rel.toPhoneNumber else rel.fromPhoneNumber
                        val userId = rel.toUserId
                        
                        // FCM 토큰 가져오기
                        val fcmToken = if (userId.isNotBlank()) {
                            contactRepository.getUserFCMToken(userId)
                        } else null
                        
                        guardians.add(Contact(
                            id = rel.id.toLongOrNull() ?: 0L,
                            userId = userId.toLongOrNull(),
                            name = rel.name,
                            phoneNumber = phone,
                            fcmToken = fcmToken
                        ))
                    }
                    
                    _selectedGuardians.value = guardians
                    Log.d("HomeViewModel", "수락된 보호자 ${guardians.size}명 로드 완료 (FCM 토큰 포함)")
                } else {
                    Log.d("HomeViewModel", "토큰이 없어 보호자 목록을 로드하지 않습니다")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "보호자 목록 로드 실패: ${e.message}", e)
                // 실패 시 더미 데이터 유지
            }
        }
    }

    private fun fetchCriminals(userLatLng: LatLng) {
        viewModelScope.launch {
            try {
                _isCriminalsLoading.value = true
                if (tokenManager.hasValidToken()) {
                    val criminals = criminalRepository.getNearbyCriminals(
                        lat = userLatLng.latitude,
                        lon = userLatLng.longitude,
                        radius = 1000
                    )
                    _preloadedCriminals.value = criminals
                    Log.d("HomeViewModel", "범죄자 ${criminals.size}개 사전 로드 완료")
                } else {
                    Log.d("HomeViewModel", "토큰이 없어 범죄자 정보를 미리 로드하지 않습니다")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "범죄자 정보 사전 로드 실패: ${e.message}", e)
                _preloadedCriminals.value = emptyList()
            } finally {
                _isCriminalsLoading.value = false
            }
        }
    }

    fun toggleMapMarkerMode() {
        _mapMarkerMode.value = if (_mapMarkerMode.value == MapMarkerMode.SAFETY_BELL_ONLY) {
            MapMarkerMode.SAFETY_BELL_AND_CRIMINALS
        } else {
            MapMarkerMode.SAFETY_BELL_ONLY
        }
        Log.d("HomeViewModel", "마커 모드 전환: ${_mapMarkerMode.value}")
    }

    fun setSelectedEmergencyBellDetail(detail: EmergencyBellDetail?) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedEmergencyBellDetail = detail)
        }
    }

    fun getEmergencyBellDetail(objtId: Int) {
        viewModelScope.launch {
            try {
                val detail = emergencyBellRepository.getEmergencyBellDetail(objtId)

                val currentState = _uiState.value
                val distanceFromNearbyList = if (currentState is HomeUiState.Success) {
                    currentState.emergencyBells.find { it.id == objtId }?.distance
                } else null

                val detailWithDistance = detail.copy(distance = distanceFromNearbyList)

                Log.d("HomeViewModel", "안전벨 상세 정보: ${detail.detail}, 거리: ${distanceFromNearbyList?.let { "${it.toInt()}m" } ?: "알 수 없음"}")
                setSelectedEmergencyBellDetail(detailWithDistance)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "안전벨 상세 정보 가져오기 실패", e)
                setSelectedEmergencyBellDetail(null)
            }
        }
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

    // ✅ 긴급 신고 메시지를 보내는 함수 추가
    fun sendEmergencyAlert(guardians: List<Contact>, message: String) {
        viewModelScope.launch {
            try {
                // 현재 위치 정보를 가져오기
                val currentState = _uiState.value
                val currentLocation = if (currentState is HomeUiState.Success) {
                    currentState.userLatLng
                } else {
                    DEFAULT_LAT_LNG
                }
                
                val myUserId = "userId_123" // TODO: 실제 사용자 ID 가져오기

                guardians.forEach { contact ->
                    val recipientToken = contact.fcmToken ?: return@forEach // 토큰이 없으면 건너뜀

                    emergencyRepository.sendEmergencyAlert(
                        recipientToken = recipientToken,
                        senderId = myUserId,
                        message = message,
                        lat = currentLocation.latitude,
                        lon = currentLocation.longitude
                    )
                }
                
                Log.d("HomeViewModel", "긴급 신고 메시지 전송 성공: ${guardians.size}명에게 전송")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "긴급 신고 메시지 전송 실패", e)
            }
        }
    }
}