package com.selfbell.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.ContactRepository
import com.selfbell.domain.repository.AuthRepository
import com.selfbell.domain.model.Criminal
import com.selfbell.domain.model.CriminalDetail
import com.selfbell.domain.model.EmergencyBell
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.repository.CriminalRepository
import com.selfbell.domain.repository.EmergencyBellRepository
import com.selfbell.data.repository.impl.TokenManager
import com.selfbell.domain.model.SosMessageRequest
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
    private val emergencyRepository: EmergencyBellRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _cameraTargetLatLng = MutableStateFlow<LatLng?>(null)
    val cameraTargetLatLng: StateFlow<LatLng?> = _cameraTargetLatLng.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResultMessage = MutableStateFlow<String?>(null)
    val searchResultMessage: StateFlow<String?> = _searchResultMessage.asStateFlow()

    private val _mapMarkerMode = MutableStateFlow(MapMarkerMode.SAFETY_BELL_ONLY)
    val mapMarkerMode: StateFlow<MapMarkerMode> = _mapMarkerMode.asStateFlow()

    private val _preloadedCriminals = MutableStateFlow<List<Criminal>>(emptyList())

    val criminals: StateFlow<List<Criminal>> = _preloadedCriminals.asStateFlow()

    private val _isCriminalsLoading = MutableStateFlow(false)
    val isCriminalsLoading: StateFlow<Boolean> = _isCriminalsLoading.asStateFlow()

    // ✅ 범죄자 정보를 한 번만 로드하기 위한 플래그 추가
    private var hasLoadedInitialCriminals = false

    private val _selectedGuardians = MutableStateFlow(
        listOf(
            Contact(1L, null, "엄마", "01011112222"),
            Contact(2L, null, "아빠", "01033334444"),
        )
    )
    val selectedGuardians: StateFlow<List<Contact>> = _selectedGuardians.asStateFlow()

    private val _guardians = MutableStateFlow<List<Contact>>(emptyList())
    val guardians: StateFlow<List<Contact>> = _guardians.asStateFlow()

    private val _messageTemplates = MutableStateFlow(
        listOf(
            "위급 상황입니다. 제 위치를 확인해주세요.",
            "현재 위험에 처해있습니다. 도움을 요청합니다."
        )
    )

    private val _selectedCriminalDetail = MutableStateFlow<CriminalDetail?>(null)
    val selectedCriminalDetail: StateFlow<CriminalDetail?> = _selectedCriminalDetail.asStateFlow()


    init {
        startHomeLocationStream()
        loadAcceptedGuardians()
        viewModelScope.launch {
            _preloadedCriminals.collectLatest { criminalsList ->
                val current = _uiState.value
                if (current is HomeUiState.Success) {
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

                    // ✅✅✅ 문제의 로직 수정 ✅✅✅
                    // 불안정한 UI 상태 체크 대신, 플래그를 사용하여 최초 한 번만 호출하도록 변경
                    if (!hasLoadedInitialCriminals) {
                        fetchCriminals(userLatLng)
                        hasLoadedInitialCriminals = true
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
                    Log.d("HomeViewModel", "=== loadAcceptedGuardians 시작 ===")
                    val relationships = contactRepository.getContactsFromServer(status = "ACCEPTED", page = 0, size = 100)
                    Log.d("HomeViewModel", "서버에서 받은 관계 데이터: ${relationships.size}개")
                    
                    val guardians = mutableListOf<Contact>()
                    
                    for (rel in relationships) {
                        Log.d("HomeViewModel", "관계 데이터 분석: id=${rel.id}, name=${rel.name}")
                        Log.d("HomeViewModel", "  - toUserId: '${rel.toUserId}' (길이: ${rel.toUserId.length})")
                        Log.d("HomeViewModel", "  - toPhoneNumber: '${rel.toPhoneNumber}'")
                        Log.d("HomeViewModel", "  - fromPhoneNumber: '${rel.fromPhoneNumber}'")
                        
                        val phone = if (rel.toPhoneNumber.isNotBlank()) rel.toPhoneNumber else rel.fromPhoneNumber
                        
                        // userId 설정 로직: rel.toUserId가 비어있으면 전화번호 기반으로 임시 userId 생성
                        val finalUserId = if (rel.toUserId.isNotBlank()) {
                            // rel.toUserId가 있으면 그 값을 사용
                            val parsedUserId = rel.toUserId.toLongOrNull()
                            Log.d("HomeViewModel", "  - rel.toUserId 사용: $parsedUserId")
                            parsedUserId
                        } else {
                            // rel.toUserId가 비어있으면 전화번호 기반으로 임시 userId 생성
                            val tempUserId = phone.hashCode().toLong().let { if (it < 0) -it else it }
                            Log.d("HomeViewModel", "  - 전화번호 기반 임시 userId 생성: $tempUserId (전화번호: $phone)")
                            tempUserId
                        }
                        
                        Log.d("HomeViewModel", "  - 최종 설정된 userId: $finalUserId")
                        
                        guardians.add(Contact(
                            id = rel.id.toLongOrNull() ?: 0L,
                            userId = finalUserId,
                            name = rel.name,
                            phoneNumber = phone
                        ))
                    }
                    
                    _guardians.value = guardians
                    _selectedGuardians.value = guardians
                    Log.d("HomeViewModel", "수락된 보호자 ${guardians.size}명 로드 완료")
                    Log.d("HomeViewModel", "userId가 있는 보호자: ${guardians.count { it.userId != null }}명")
                    Log.d("HomeViewModel", "userId가 null인 보호자: ${guardians.count { it.userId == null }}명")
                } else {
                    Log.d("HomeViewModel", "토큰이 없어 보호자 목록을 로드하지 않습니다")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "보호자 목록 로드 실패: ${e.message}", e)
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

    fun setSelectedCriminalDetail(detail: CriminalDetail?) {
        _selectedCriminalDetail.value = detail
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

    fun sendEmergencyAlert(selectedGuardians: List<Contact>, message: String) {
        Log.d("HomeViewModel", "=== sendEmergencyAlert 함수 시작 ===")
        Log.d("HomeViewModel", "입력받은 보호자: ${selectedGuardians.size}명")
        selectedGuardians.forEachIndexed { index, contact ->
            Log.d("HomeViewModel", "보호자 ${index + 1}: ${contact.name} (ID: ${contact.id}, userId: ${contact.userId}, 전화번호: ${contact.phoneNumber})")
        }
        Log.d("HomeViewModel", "입력받은 메시지: '$message'")
        
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "1단계: 현재 사용자 위치 가져오기")
                val currentState = _uiState.value
                val currentLocation = if (currentState is HomeUiState.Success) {
                    currentState.userLatLng
                } else {
                    DEFAULT_LAT_LNG
                }
                Log.d("HomeViewModel", "현재 위치: lat=${currentLocation.latitude}, lon=${currentLocation.longitude}")

                Log.d("HomeViewModel", "2단계: 보호자 userId 추출")
                val receiverIds = selectedGuardians
                    .mapNotNull { it.userId }
                    .filter { it > 0 }
                
                Log.d("HomeViewModel", "추출된 receiverIds: $receiverIds")
                
                if (receiverIds.isEmpty()) {
                    Log.w("HomeViewModel", "유효한 receiver ID가 없습니다")
                    return@launch
                }

                Log.d("HomeViewModel", "3단계: SosMessageRequest 객체 생성")
                val request = SosMessageRequest(
                    receiverUserIds = receiverIds,
                    templateId = 1,
                    message = message,
                    lat = currentLocation.latitude,
                    lon = currentLocation.longitude
                )
                Log.d("HomeViewModel", "생성된 SosMessageRequest: $request")

                Log.d("HomeViewModel", "4단계: emergencyBellRepository.sendSosMessage() 호출")
                Log.d("HomeViewModel", "API 호출 시작: POST /api/v1/sos/messages")
                
                val response = emergencyBellRepository.sendSosMessage(request)

                Log.d("HomeViewModel", "=== SOS 메시지 전송 성공! ===")
                Log.d("HomeViewModel", "응답 ID: ${response.id}")
                Log.d("HomeViewModel", "전송된 수: ${response.sentCount}")
                Log.d("HomeViewModel", "전체 응답: $response")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "=== SOS 메시지 전송 실패! ===", e)
                Log.e("HomeViewModel", "에러 타입: ${e.javaClass.simpleName}")
                Log.e("HomeViewModel", "에러 메시지: ${e.message}")
                if (e is retrofit2.HttpException) {
                    Log.e("HomeViewModel", "HTTP 에러 코드: ${e.code()}")
                    Log.e("HomeViewModel", "HTTP 에러 메시지: ${e.message()}")
                }
            }
        }
    }

    // ✅ 상세 정보 상태를 모두 초기화하는 함수 추가
    fun clearDetails() {
        setSelectedEmergencyBellDetail(null)
        setSelectedCriminalDetail(null)
    }
}