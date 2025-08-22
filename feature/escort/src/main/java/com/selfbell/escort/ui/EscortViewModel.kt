// feature/escort/ui/EscortViewModel.kt
package com.selfbell.escort.ui

import android.content.ContentResolver
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
import com.selfbell.core.model.Contact // 이 import를 사용합니다.
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
import com.selfbell.domain.model.ContactRelationship
import java.time.LocalDateTime
import java.time.LocalTime
import retrofit2.HttpException


enum class EscortFlowState {
    SETUP,
    GUARDIAN_SELECTION,
    IN_PROGRESS
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
    private val contactRepository: ContactRepository
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

    // ✅ 즐겨찾기 목록을 저장할 상태
    private val _favoriteAddresses = MutableStateFlow<List<FavoriteAddress>>(emptyList())
    val favoriteAddresses = _favoriteAddresses.asStateFlow()

    // ✅ 세션 시작 후 보호자 공유 UI 표시 여부를 관리하는 상태
    private val _showGuardianShareSheet = MutableStateFlow(false)
    val showGuardianShareSheet = _showGuardianShareSheet.asStateFlow()

    // ✅ 예상 도착 시간 상태 추가
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


    init {
        loadContacts()
        loadAcceptedFriends() // 친구 목록 로드 추가
        checkCurrentSession() // ✅ ViewModel 생성 시 진행 중인 세션 확인
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

    // ✅ 즐겨찾기 선택 시, 목적지를 업데이트하고 isDestinationSelected를 true로 변경
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
        _showTimeInputModal.value = true // 시간 입력 모달 표시
        Log.d(
            "EscortViewModel",
            "[onFavoriteAddressSelected] destination=${_destinationLocation.value.name}, lat=${_destinationLocation.value.latLng.latitude}, lon=${_destinationLocation.value.latLng.longitude}, isDestinationSelected=${_isDestinationSelected.value}, showTimeInputModal=${_showTimeInputModal.value}"
        )
    }

    // ✅ 직접 주소 입력 완료 후 호출될 함수 (가정)
    fun onDirectAddressSelected(name: String, latLng: LatLng) {
        Log.d(
            "EscortViewModel",
            "[onDirectAddressSelected] 입력 name=$name, lat=${latLng.latitude}, lon=${latLng.longitude}"
        )
        _destinationLocation.value = LocationState(name, latLng)
        _isDestinationSelected.value = true
        _showTimeInputModal.value = true // 시간 입력 모달 표시
        Log.d(
            "EscortViewModel",
            "[onDirectAddressSelected] destination=${_destinationLocation.value.name}, lat=${_destinationLocation.value.latLng.latitude}, lon=${_destinationLocation.value.latLng.longitude}, isDestinationSelected=${_isDestinationSelected.value}, showTimeInputModal=${_showTimeInputModal.value}"
        )
    }

    private fun checkCurrentSession() {
        viewModelScope.launch {
            // 1. 먼저 토큰 상태 확인
            if (!tokenManager.hasValidToken()) {
                Log.d("EscortViewModel", "유효한 토큰이 없습니다. SETUP 상태로 초기화")
                _sessionId.value = null
                _isSessionActive.value = false
                _escortFlowState.value = EscortFlowState.SETUP
                return@launch
            }

            // 2. 서버에서 현재 세션 확인
            try {
                val currentSession = safeWalkRepository.getCurrentSafeWalk()
                if (currentSession != null) {
                    Log.d("EscortViewModel", "서버에서 진행 중인 세션 발견: ${currentSession.sessionId}")
                    // 진행 중인 세션을 그대로 이어서 IN_PROGRESS 화면으로 전환
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
                // 오류 발생 시 SETUP 상태로 초기화
                _sessionId.value = null
                _isSessionActive.value = false
                _escortFlowState.value = EscortFlowState.SETUP
            }
        }
    }

    // ✅ 위치 추적 시작 함수
    private fun startLocationTracking() {
        viewModelScope.launch {
            try {
                locationTracker.getLocationUpdates().collectLatest { location ->
                    updateLocationTrack(
                        location.latitude,
                        location.longitude,
                        location.accuracy.toDouble()
                    )
                }
            } catch (e: Exception) {
                Log.e("EscortViewModel", "위치 추적 시작 실패", e)
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

    // ✅ 타이머 또는 도착 예정 시간이 변경될 때마다 활성화 여부 체크
    fun setTimerMinutes(minutes: Int) {
        _timerMinutes.value = minutes
        checkSetupCompletion()
    }

    // ✅ 예상 도착 시간 설정 함수
    fun setExpectedArrivalTime(time: LocalTime) {
        _expectedArrivalTime.value = time
        checkSetupCompletion()
    }

    // ✅ 선택된 보호자들로 안심귀가 시작 함수
    fun startSafeWalk() {
        viewModelScope.launch {
            try {
                // ✅ 토큰 상태 확인
                if (!tokenManager.hasValidToken()) {
                    Log.e("EscortViewModel", "유효한 토큰이 없습니다. 로그인이 필요합니다.")
                    // TODO: 사용자에게 로그인 필요 알림
                    return@launch
                }

                val currentToken = tokenManager.getAccessToken()
                Log.d("EscortViewModel", "현재 토큰: $currentToken")

                // ✅ 선택된 연락처의 전화번호를 친구 목록과 매칭하여 userId 추출
                val guardianIds = _selectedGuardians.value.mapNotNull { it.userId }

                Log.d("EscortViewModel", "Guardian IDs: $guardianIds") // 이제 여기에 ID가 표시됩니다.


                // 예상 도착 시간 계산
                val expectedArrival: LocalDateTime? = when (_arrivalMode.value) {
                    ArrivalMode.TIMER -> LocalDateTime.now()
                        .plusMinutes(_timerMinutes.value.toLong())

                    ArrivalMode.SCHEDULED_TIME -> _expectedArrivalTime.value?.let {
                        LocalDateTime.now().withHour(it.hour).withMinute(it.minute)
                    }
                }

                // ✅ 디버깅을 위한 로그 추가
                Log.d("EscortViewModel", "SafeWalk 세션 생성 시작")
                Log.d("EscortViewModel", "Guardian IDs: $guardianIds")
                Log.d("EscortViewModel", "Expected Arrival: $expectedArrival")
                Log.d("EscortViewModel", "Timer Minutes: ${_timerMinutes.value}")
                Log.d("EscortViewModel", "Arrival Mode: ${_arrivalMode.value}")

                val session = safeWalkRepository.createSafeWalkSession(
                    originLat = _startLocation.value.latLng.latitude,
                    originLon = _startLocation.value.latLng.longitude,
                    originAddress = _startLocation.value.name,
                    destinationLat = _destinationLocation.value.latLng.latitude,
                    destinationLon = _destinationLocation.value.latLng.longitude,
                    destinationAddress = _destinationLocation.value.name,
                    expectedArrival = expectedArrival,
                    timerMinutes = if (_arrivalMode.value == ArrivalMode.TIMER) _timerMinutes.value else null,
                    guardianIds = guardianIds
                )

                // 성공 시 상태 업데이트
                _sessionId.value = session.sessionId
                _isSessionActive.value = true
                _escortFlowState.value = EscortFlowState.IN_PROGRESS
                // 보호자 선택 초기화
                _selectedGuardians.value = emptySet()

                // ✅ WebSocket 연결 - 실제 액세스 토큰 사용
                tokenManager.getAccessToken()?.let { token ->
                    stompManager.connect(token, session.sessionId)
                }

                // 위치 추적 시작
                startLocationTracking()
                Log.d("EscortViewModel", "SafeWalk 세션 생성 성공: ${session.sessionId}")
            } catch (e: Exception) {
                Log.e("EscortViewModel", "세션 생성 실패", e)
                // ✅ 더 자세한 에러 정보 로깅
                when (e) {
                    is HttpException -> {
                        Log.e("EscortViewModel", "HTTP 에러: ${e.code()}")
                        Log.e("EscortViewModel", "에러 응답: ${e.response()?.errorBody()?.string()}")

                        // 401 또는 403 오류 시 로그만 남기고 토큰은 유지
                        if (e.code() == 401 || e.code() == 403) {
                            Log.e("EscortViewModel", "토큰이 만료되었거나 권한이 없습니다. (토큰 유지)")
                        }
                    }

                    else -> {
                        Log.e("EscortViewModel", "기타 에러: ${e.message}")
                    }
                }
            }
        }
    }

    // ✅ 안심귀가 종료 함수
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
                    // 성공/실패와 관계없이 로컬 상태 초기화
                    _isSessionActive.value = false
                    _sessionId.value = null
                    _escortFlowState.value = EscortFlowState.SETUP // 초기 설정 화면으로 복귀
                    _isDestinationSelected.value = false // 목적지 선택 초기화
                    _selectedGuardians.value = emptySet() // 선택된 보호자 초기화
                    _isStartButtonEnabled.value = false // 버튼 비활성화
                    locationTracker.stopLocationUpdates()
                    stompManager.disconnect()
                }
            }
        }
    }

    // ✅ 위치 트랙 업데이트 함수
    fun updateLocationTrack(lat: Double, lon: Double, accuracy: Double) {
        _sessionId.value?.let { sessionId ->
            viewModelScope.launch {
                try {
                    val success =
                        safeWalkRepository.uploadLocationTrack(sessionId, lat, lon, accuracy)
                    if (!success) {
                        Log.w("EscortViewModel", "위치 트랙 업데이트 실패")
                    }
                    stompManager.sendLocation(sessionId, lat, lon)

                } catch (e: Exception) {
                    Log.e("EscortViewModel", "위치 트랙 업데이트 중 오류", e)
                }
            }
        }
    }

    // ✅ 즐겨찾기 목록을 불러오는 함수
    private fun loadFavoriteAddresses() {
        viewModelScope.launch {
            try {
                _favoriteAddresses.value = FavoriteAddressRepository.getFavoriteAddresses()
            } catch (e: Exception) {
                Log.e("EscortViewModel", "즐겨찾기 주소 로딩 실패", e)
            }
        }
    }

    fun updateStartLocation(name: String, latLng: LatLng) {
        _startLocation.value = LocationState(name, latLng)
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

    // ✅ 보호자 공유 UI 토글 함수
    fun toggleGuardianShareSheet() {
        _showGuardianShareSheet.value = !_showGuardianShareSheet.value
    }

    // ✅ 시간 입력 모달 닫기 함수
    fun closeTimeInputModal() {
        _showTimeInputModal.value = false
        _isDestinationSelected.value = false // 목적지 선택 상태를 초기화
        _isSetupComplete.value = false // 설정 완료 상태도 초기화
        // 도착지 이름도 초기값으로 되돌림
        _destinationLocation.value = LocationState("메인 주소 (더미)", LatLng(37.5665, 126.9780))
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
                                    userId = null, // 👈 기기 연락처에는 userId가 없으므로 null
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
                // 권한이 없어도 앱이 크래시되지 않도록 빈 리스트로 설정
                _allContacts.value = emptyList()
            } catch (e: Exception) {
                Log.e("EscortViewModel", "연락처 로딩 실패: ${e.message}")
                _allContacts.value = emptyList()
            }
        }
    }

    // 친구 목록을 가져오는 함수
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
        // ViewModel이 정리될 때 위치 추적 중지
        locationTracker.stopLocationUpdates()
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