// feature/escort/ui/EscortViewModel.kt
package com.selfbell.escort.ui

import com.selfbell.escort.ui.SafeWalkService
import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.selfbell.core.model.Contact
import com.selfbell.domain.model.SessionEndReason
import com.selfbell.domain.repository.SafeWalkRepository
import com.selfbell.data.repository.impl.TokenManager
import com.selfbell.core.location.LocationTracker
import com.selfbell.data.api.StompManager
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.model.FavoriteAddress
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.FavoriteAddressRepository
import com.selfbell.domain.repository.ContactRepository
import com.selfbell.domain.repository.ReverseGeocodingRepository
import com.selfbell.domain.model.ContactRelationship
import dagger.hilt.android.internal.Contexts.getApplication
import java.time.LocalDateTime
import java.time.LocalTime
import retrofit2.HttpException


enum class EscortFlowState {
    SETUP,
    GUARDIAN_SELECTION,
    IN_PROGRESS
}

private enum class DestinationSelectionType {
    NONE, FAVORITE, DIRECT
}

@HiltViewModel
class EscortViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
    private val safeWalkRepository: SafeWalkRepository,
    private val FavoriteAddressRepository: FavoriteAddressRepository,
    private val addressRepository: AddressRepository,
    private val locationTracker: LocationTracker,
    private val tokenManager: TokenManager,
    private val contactRepository: ContactRepository,
    private val reverseGeocodingRepository: ReverseGeocodingRepository,
    private val application: Application // ✅ Application Context 주입
) : ViewModel() {

    private val stompManager = StompManager()

    // 출발지/도착지 상태
    private val _startLocation = MutableStateFlow(LocationState("현재 위치", LatLng(37.5665, 126.9780)))
    val startLocation = _startLocation.asStateFlow()
    private val _destinationLocation =
        MutableStateFlow(LocationState("메인 주소 (더미)", LatLng(37.5665, 126.9780)))
    val destinationLocation = _destinationLocation.asStateFlow()
    private val _arrivalMode = MutableStateFlow(ArrivalMode.TIMER)
    val arrivalMode = _arrivalMode.asStateFlow()
    private val _timerMinutes = MutableStateFlow(30)
    val timerMinutes = _timerMinutes.asStateFlow()

    private val _favoriteAddresses = MutableStateFlow<List<FavoriteAddress>>(emptyList())
    val favoriteAddresses = _favoriteAddresses.asStateFlow()

    private val _showGuardianShareSheet = MutableStateFlow(false)
    val showGuardianShareSheet = _showGuardianShareSheet.asStateFlow()

    private val _expectedArrivalTime = MutableStateFlow<LocalTime?>(null)
    val expectedArrivalTime = _expectedArrivalTime.asStateFlow()

    // 연락처 관련 상태 추가
    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 친구 목록 상태 추가
    private val _acceptedFriends = MutableStateFlow<List<ContactRelationship>>(emptyList())
    val acceptedFriends: StateFlow<List<ContactRelationship>> = _acceptedFriends

    // ✅ 세션 관리 상태
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()
    private val _sessionId = MutableStateFlow<Long?>(null)

    // ✅ 보호자 선택 관련 상태 추가
    private val _selectedGuardians = MutableStateFlow<Set<Contact>>(emptySet())
    val selectedGuardians: StateFlow<Set<Contact>> = _selectedGuardians.asStateFlow()
    private val _escortFlowState = MutableStateFlow(EscortFlowState.SETUP)
    val escortFlowState = _escortFlowState.asStateFlow()

    // ✅ 목적지가 선택되었는지 여부를 관리하는 상태
    private val _isDestinationSelected = MutableStateFlow(false)
    val isDestinationSelected = _isDestinationSelected.asStateFlow()

    // ✅ '출발하기' 버튼 활성화 여부
    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete = _isSetupComplete.asStateFlow()

    // ✅ 2. '출발하기' 버튼의 활성화 상태를 별도로 관리
    private val _isStartButtonEnabled = MutableStateFlow(false)
    val isStartButtonEnabled = _isStartButtonEnabled.asStateFlow()

    // ✅ 주소 입력 후 시간 입력 모달 표시 여부
    private val _showTimeInputModal = MutableStateFlow(false)
    val showTimeInputModal = _showTimeInputModal.asStateFlow()

    // ✅ [추가] 목적지 선택 방식을 저장할 상태 변수
    private val _destinationSelectionType = MutableStateFlow(DestinationSelectionType.NONE)


    init {
        trackCurrentUserLocationForSetup()
        loadContacts()
        loadAcceptedFriends()
        checkCurrentSession()
        loadFavoriteAddresses()
        observeAddressSearchResult()
    }

    private fun observeAddressSearchResult() {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("address_name", null).collect { name ->
                val lat = savedStateHandle.get<Double>("address_lat")
                val lon = savedStateHandle.get<Double>("address_lon")

                if (name != null && lat != null && lon != null) {
                    Log.d(
                        "EscortViewModel",
                        "[observeAddressSearchResult] 수신됨 name=$name, lat=$lat, lon=$lon"
                    )
                    onDirectAddressSelected(name, LatLng(lat, lon))
                    Log.d("EscortViewModel", "[observeAddressSearchResult] onDirectAddressSelected 호출 완료")
                    savedStateHandle["address_name"] = null
                    savedStateHandle["address_lat"] = null
                    savedStateHandle["address_lon"] = null
                    Log.d("EscortViewModel", "[observeAddressSearchResult] SavedStateHandle 키 초기화 완료")
                }
            }
        }
    }

    private fun checkSetupCompletion() {
        val isTimeSet = (_arrivalMode.value == ArrivalMode.TIMER && _timerMinutes.value > 0) ||
                (_arrivalMode.value == ArrivalMode.SCHEDULED_TIME && _expectedArrivalTime.value != null)

        _isSetupComplete.value = _isDestinationSelected.value && isTimeSet

        if (_isDestinationSelected.value && isTimeSet) {
            _escortFlowState.value = EscortFlowState.GUARDIAN_SELECTION
        }
    }

    fun onFavoriteAddressSelected(favoriteAddress: FavoriteAddress) {
        Log.d(
            "EscortViewModel",
            "[onFavoriteAddressSelected] name=${favoriteAddress.name}, lat=${favoriteAddress.lat}, lon=${favoriteAddress.lon}"
        )
        _destinationLocation.value = LocationState(
            name = favoriteAddress.name,
            latLng = LatLng(favoriteAddress.lat, favoriteAddress.lon)
        )
        _isDestinationSelected.value = true
        _showTimeInputModal.value = true
        _destinationSelectionType.value = DestinationSelectionType.FAVORITE
        Log.d(
            "EscortViewModel",
            "[onFavoriteAddressSelected] destination=${_destinationLocation.value.name}, lat=${_destinationLocation.value.latLng.latitude}, lon=${_destinationLocation.value.latLng.longitude}, isDestinationSelected=${_isDestinationSelected.value}, showTimeInputModal=${_showTimeInputModal.value}, destinationSelectionType=${_destinationSelectionType.value}"
        )
    }

    fun onDirectAddressSelected(name: String, latLng: LatLng) {
        Log.d(
            "EscortViewModel",
            "[onDirectAddressSelected] 입력 name=$name, lat=${latLng.latitude}, lon=${latLng.longitude}"
        )
        _destinationLocation.value = LocationState(name, latLng)
        _isDestinationSelected.value = true
        _showTimeInputModal.value = true
        _destinationSelectionType.value = DestinationSelectionType.DIRECT
        Log.d(
            "EscortViewModel",
            "[onDirectAddressSelected] destination=${_destinationLocation.value.name}, lat=${_destinationLocation.value.latLng.latitude}, lon=${_destinationLocation.value.latLng.longitude}, isDestinationSelected=${_isDestinationSelected.value}, showTimeInputModal=${_showTimeInputModal.value}, destinationSelectionType=${_destinationSelectionType.value}"
        )
    }

    private fun checkCurrentSession() {
        viewModelScope.launch {
            if (!tokenManager.hasValidToken()) {
                Log.d("EscortViewModel", "유효한 토큰이 없습니다. SETUP 상태로 초기화")
                _sessionId.value = null
                _isSessionActive.value = false
                _escortFlowState.value = EscortFlowState.SETUP
                return@launch
            }

            try {
                val currentSession = safeWalkRepository.getCurrentSafeWalk()
                if (currentSession != null) {
                    Log.d("EscortViewModel", "서버에서 진행 중인 세션 발견: ${currentSession.sessionId}")
                    _sessionId.value = currentSession.sessionId
                    _isSessionActive.value = true
                    _escortFlowState.value = EscortFlowState.IN_PROGRESS
                } else {
                    Log.d("EscortViewModel", "서버에 진행 중인 세션이 없습니다. SETUP 상태로 초기화")
                    _sessionId.value = null
                    _isSessionActive.value = false
                    _escortFlowState.value = EscortFlowState.SETUP
                }
            } catch (e: Exception) {
                Log.e("EscortViewModel", "현재 세션 확인 중 오류 발생", e)
                _sessionId.value = null
                _isSessionActive.value = false
                _escortFlowState.value = EscortFlowState.SETUP
            }
        }
    }

    // ✅ 4. 보호자 선택/해제 시 '출발하기' 버튼 활성화 여부 업데이트
    fun toggleGuardianSelection(contact: Contact) {
        val currentSelected = _selectedGuardians.value.toMutableSet()
        if (currentSelected.contains(contact)) {
            currentSelected.remove(contact)
        } else {
            currentSelected.add(contact)
        }
        _selectedGuardians.value = currentSelected
        _isStartButtonEnabled.value = currentSelected.isNotEmpty()
    }

    fun setTimerMinutes(minutes: Int) {
        _timerMinutes.value = minutes
        checkSetupCompletion()
    }

    fun setExpectedArrivalTime(time: LocalTime) {
        _expectedArrivalTime.value = time
        checkSetupCompletion()
    }

    fun returnToTimeSetup() {
        _escortFlowState.value = EscortFlowState.SETUP
        _showTimeInputModal.value = true
        _selectedGuardians.value = emptySet()
        _isStartButtonEnabled.value = false
    }

    fun startSafeWalk() {
        viewModelScope.launch {
            try {
                if (!tokenManager.hasValidToken()) {
                    Log.e("EscortViewModel", "유효한 토큰이 없습니다. 로그인이 필요합니다.")
                    // TODO: 사용자에게 로그인 필요 알림
                    return@launch
                }

                val guardianIds = _selectedGuardians.value.mapNotNull { it.userId }

                val expectedArrival: LocalDateTime? = when (_arrivalMode.value) {
                    ArrivalMode.TIMER -> LocalDateTime.now()
                        .plusMinutes(_timerMinutes.value.toLong())

                    ArrivalMode.SCHEDULED_TIME -> _expectedArrivalTime.value?.let { selectedTime ->
                        var selectedDateTime = LocalDateTime.now().withHour(selectedTime.hour).withMinute(selectedTime.minute)
                        // ✅ 선택된 시간이 현재 시간보다 이전이면 하루를 더해줌
                        if (selectedDateTime.isBefore(LocalDateTime.now())) {
                            selectedDateTime = selectedDateTime.plusDays(1)
                        }
                        selectedDateTime
                    }
                }

                // ✅ 수정됨: 선택 방식에 따라 destinationName 결정 ("직접입력"으로 변경)
                val destinationName = when (_destinationSelectionType.value) {
                    DestinationSelectionType.FAVORITE -> _destinationLocation.value.name
                    DestinationSelectionType.DIRECT -> "직접입력"
                    else -> _destinationLocation.value.name
                }

                val session = safeWalkRepository.createSafeWalkSession(
                    originLat = _startLocation.value.latLng.latitude,
                    originLon = _startLocation.value.latLng.longitude,
                    originAddress = _startLocation.value.name,
                    destinationLat = _destinationLocation.value.latLng.latitude,
                    destinationLon = _destinationLocation.value.latLng.longitude,
                    destinationAddress = _destinationLocation.value.name,
                    destinationName = destinationName,
                    expectedArrival = expectedArrival,
                    timerMinutes = if (_arrivalMode.value == ArrivalMode.TIMER) _timerMinutes.value else null,
                    guardianIds = guardianIds
                )

                _sessionId.value = session.sessionId
                _isSessionActive.value = true
                _escortFlowState.value = EscortFlowState.IN_PROGRESS
                _selectedGuardians.value = emptySet()

                tokenManager.getAccessToken()?.let { token ->
                    stompManager.connect(token, session.sessionId)
                }

                // ✅ 수정됨: ViewModel에서 직접 위치 추적을 시작하는 대신, Service를 시작
                val serviceIntent = Intent(application, SafeWalkService::class.java).apply {
                    putExtra("SESSION_ID", session.sessionId)
                }
                application.startForegroundService(serviceIntent)
                Log.d("EscortViewModel", "SafeWalkService 시작 요청")

            } catch (e: Exception) {
                Log.e("EscortViewModel", "세션 생성 실패", e)
            }
        }
    }

    fun endSafeWalk() {
        _sessionId.value?.let { currentSessionId ->
            viewModelScope.launch {
                try {
                    val success = safeWalkRepository.endSafeWalkSession(
                        currentSessionId,
                        SessionEndReason.MANUAL
                    )
                    if (success) {
                        Log.d("EscortViewModel", "안심귀가 세션 종료 성공")
                    } else {
                        Log.w("EscortViewModel", "안심귀가 세션 종료 실패")
                    }
                } catch (e: Exception) {
                    Log.e("EscortViewModel", "안심귀가 세션 종료 중 오류", e)
                } finally {
                    _isSessionActive.value = false
                    _sessionId.value = null
                    _escortFlowState.value = EscortFlowState.SETUP
                    _isDestinationSelected.value = false
                    _showTimeInputModal.value = false
                    _isSetupComplete.value = false
                    _timerMinutes.value = 30
                    _expectedArrivalTime.value = null
                    _selectedGuardians.value = emptySet()
                    _isStartButtonEnabled.value = false
                    _destinationSelectionType.value = DestinationSelectionType.NONE

                    // ✅ 서비스를 중단하는 Intent 생성
                    val serviceIntent = Intent(application, SafeWalkService::class.java)
                    // ✅ 서비스 중단
                    application.stopService(serviceIntent)
                    Log.d("EscortViewModel", "SafeWalkService 중단 요청")

                    stompManager.disconnect()
                }
            }
        }
    }

    // ✅ 삭제됨: 위치 트랙 업데이트 함수는 이제 Service에서 직접 호출
    /*
    fun updateLocationTrack(lat: Double, lon: Double, accuracy: Double) {
        _sessionId.value?.let { sessionId ->
            viewModelScope.launch {
                try {
                    safeWalkRepository.uploadLocationTrack(sessionId, lat, lon, accuracy)
                    stompManager.sendLocation(sessionId, lat, lon)
                } catch (e: Exception) {
                    Log.e("EscortViewModel", "위치 트랙 업데이트 중 오류", e)
                }
            }
        }
    }
     */

    private fun loadFavoriteAddresses() {
        viewModelScope.launch {
            try {
                _favoriteAddresses.value = FavoriteAddressRepository.getFavoriteAddresses()
            } catch (e: Exception) {
                Log.e("EscortViewModel", "즐겨찾기 주소 로딩 실패", e)
            }
        }
    }

    private fun trackCurrentUserLocationForSetup() {
        viewModelScope.launch {
            locationTracker.getLocationUpdates().collect { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)

                Log.d("EscortViewModel", "=== 현재 위치 추적 시작 ===")
                Log.d("EscortViewModel", "위치 좌표: lat=${location.latitude}, lon=${location.longitude}")
                Log.d("EscortViewModel", "위치 정확도: ${location.accuracy}m")

                val currentAddress = try {
                    Log.d("EscortViewModel", "Reverse Geocoding API 호출 시작...")
                    val address = reverseGeocodingRepository.reverseGeocode(location.latitude, location.longitude)
                    Log.d("EscortViewModel", "Reverse Geocoding API 호출 완료")
                    Log.d("EscortViewModel", "변환된 주소: $address")
                    address
                } catch (e: Exception) {
                    Log.e("EscortViewModel", "=== Reverse Geocoding 실패 ===")
                    Log.e("EscortViewModel", "에러 타입: ${e.javaClass.simpleName}")
                    Log.e("EscortViewModel", "에러 메시지: ${e.message}")
                    Log.e("EscortViewModel", "스택 트레이스: ${e.stackTraceToString()}")
                    null
                }
                val addressName = currentAddress ?: "현재 위치"
                Log.d("EscortViewModel", "=== 현재 위치 업데이트 완료 ===")
                Log.d("EscortViewModel", "최종 주소: $addressName")
                Log.d("EscortViewModel", "위치 상태 업데이트: name=$addressName, lat=${currentLatLng.latitude}, lon=${currentLatLng.longitude}")
                _startLocation.value = LocationState(addressName, currentLatLng)
                Log.d("EscortViewModel", "=== 현재 위치 추적 완료 ===")
            }
        }
    }

    fun updateDestinationLocation(name: String, latLng: LatLng) {
        Log.d(
            "EscortViewModel",
            "[updateDestinationLocation] name=$name, lat=${latLng.latitude}, lon=${latLng.longitude}"
        )
        _destinationLocation.value = LocationState(name, latLng)
        Log.d(
            "EscortViewModel",
            "[updateDestinationLocation] 상태 반영 destination=${_destinationLocation.value.name}, lat=${_destinationLocation.value.latLng.latitude}, lon=${_destinationLocation.value.latLng.longitude}"
        )
    }

    fun toggleGuardianShareSheet() {
        _showGuardianShareSheet.value = !_showGuardianShareSheet.value
    }

    fun closeTimeInputModal() {
        _showTimeInputModal.value = false
        _isDestinationSelected.value = false
        _isSetupComplete.value = false
        _destinationLocation.value = LocationState("메인 주소 (더미)", LatLng(37.5665, 126.9780))
        _destinationSelectionType.value = DestinationSelectionType.NONE
    }

    fun setArrivalMode(mode: ArrivalMode) {
        _arrivalMode.value = mode
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contactsList = mutableListOf<Contact>()
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )

                cursor?.use {
                    val idIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (it.moveToNext()) {
                        val contactId = it.getLong(idIndex)
                        val name = it.getString(nameIndex) ?: "Unknown"
                        val number = it.getString(numberIndex) ?: ""
                        if (name.isNotEmpty() && number.isNotEmpty()) {
                            contactsList.add(
                                Contact(
                                    id = contactId,
                                    userId = null,
                                    name = name,
                                    phoneNumber = number.replace("-", "").trim()
                                )
                            )
                        }
                    }
                }
                _allContacts.value = contactsList.distinctBy { it.phoneNumber }
                Log.d("EscortViewModel", "연락처 로딩 완료: ${contactsList.size}개")
            } catch (e: SecurityException) {
                Log.e("EscortViewModel", "연락처 권한이 없습니다: ${e.message}")
                _allContacts.value = emptyList()
            } catch (e: Exception) {
                Log.e("EscortViewModel", "연락처 로딩 실패: ${e.message}")
                _allContacts.value = emptyList()
            }
        }
    }

    private fun loadAcceptedFriends() {
        viewModelScope.launch {
            try {
                val friends = contactRepository.getContactsFromServer("ACCEPTED", 0, 100)
                _acceptedFriends.value = friends
                Log.d("EscortViewModel", "친구 목록 로딩 완료: ${friends.size}명")
            } catch (e: Exception) {
                Log.e("EscortViewModel", "친구 목록 로딩 실패: ${e.message}")
                _acceptedFriends.value = emptyList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ✅ 삭제됨: ViewModel에서는 더 이상 위치 추적을 중단하지 않습니다.
    }

    data class LocationState(
        val name: String,
        val latLng: LatLng
    )

    enum class ArrivalMode {
        TIMER,
        SCHEDULED_TIME
    }
}