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
import com.selfbell.domain.model.AddressModel
import com.selfbell.domain.model.FavoriteAddress
import com.selfbell.domain.repository.AddressRepository
import com.selfbell.domain.repository.FavoriteAddressRepository
import java.time.LocalDateTime
import java.time.LocalTime
import retrofit2.HttpException


enum class EscortFlowState {
    SETUP,
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
    private val tokenManager: TokenManager
) : ViewModel() {
    // 출발지/도착지 상태
    private val _startLocation = MutableStateFlow(LocationState("현재 위치", LatLng(37.5665, 126.9780)))
    val startLocation = _startLocation.asStateFlow()
    private val _destinationLocation = MutableStateFlow(LocationState("메인 주소 (더미)", LatLng(37.5665, 126.9780)))
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
    // --- ✅ 주소 검색 플로우를 위한 상태 추가 ---
    private val _isSearchingAddress = MutableStateFlow(false)
    val isSearchingAddress = _isSearchingAddress.asStateFlow()

    private val _addressSearchQuery = MutableStateFlow("")
    val addressSearchQuery = _addressSearchQuery.asStateFlow()

    private val _addressSearchResults = MutableStateFlow<List<AddressModel>>(emptyList())
    val addressSearchResults = _addressSearchResults.asStateFlow()

    // 지도 확인 단계로 넘어갈 때 사용할 선택된 주소 정보
    private val _selectedAddressForConfirmation = MutableStateFlow<AddressModel?>(null)
    val selectedAddressForConfirmation = _selectedAddressForConfirmation.asStateFlow()


    init {
        loadContacts()
        checkCurrentSession() // ✅ ViewModel 생성 시 진행 중인 세션 확인
        loadFavoriteAddresses()

        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("address_name", null).collect { name ->
                val lat = savedStateHandle.get<Double>("address_lat")
                val lon = savedStateHandle.get<Double>("address_lon")

                if (name != null && lat != null && lon != null) {
                    onDirectAddressSelected(name, LatLng(lat, lon))
                    // 처리 후에는 값을 초기화하여 중복 처리를 방지
                    savedStateHandle["address_name"] = null
                    savedStateHandle["address_lat"] = null
                    savedStateHandle["address_lon"] = null
                }
            }
        }

    }

    // TODO: UserRepository에서 즐겨찾기 주소를 가져오는 로직 필요
    // fun onFavoriteAddressClick(type: FavoriteType) { ... }

    private fun checkSetupCompletion() {
        val isTimeSet = (_arrivalMode.value == ArrivalMode.TIMER && _timerMinutes.value > 0) ||
                (_arrivalMode.value == ArrivalMode.SCHEDULED_TIME && _expectedArrivalTime.value != null)

        // ✅ 목적지와 시간이 모두 설정되어야 버튼 활성화
        _isSetupComplete.value = _isDestinationSelected.value && isTimeSet
    }

    // ✅ 즐겨찾기 선택 시, 목적지를 업데이트하고 isDestinationSelected를 true로 변경
    fun onFavoriteAddressSelected(favoriteAddress: FavoriteAddress) {
        _destinationLocation.value = LocationState(
            name = favoriteAddress.name,
            latLng = LatLng(favoriteAddress.lat, favoriteAddress.lon)
        )
        _isDestinationSelected.value = true
    }

    // ✅ 직접 주소 입력 완료 후 호출될 함수 (가정)
    fun onDirectAddressSelected(name: String, latLng: LatLng) {
        _destinationLocation.value = LocationState(name, latLng)
        _isDestinationSelected.value = true
    }

    // ✅ 진행 중인 세션 확인 함수
    private fun checkCurrentSession() {
        viewModelScope.launch {
            val currentSession = safeWalkRepository.getCurrentSafeWalk()
            if (currentSession != null) {
                _sessionId.value = currentSession.sessionId
                _isSessionActive.value = true
                startLocationTracking() // 세션이 활성화되면 위치 추적 시작
            }
        }
    }

    // ✅ 위치 추적 시작 함수
    private fun startLocationTracking() {
        viewModelScope.launch {
            try {
                locationTracker.getLocationUpdates().collectLatest { location ->
                    updateLocationTrack(location.latitude, location.longitude, location.accuracy.toDouble())
                }
            } catch (e: Exception) {
                Log.e("EscortViewModel", "위치 추적 시작 실패", e)
            }
        }
    }

    // ✅ 보호자 선택/해제 함수
    fun toggleGuardianSelection(contact: Contact) {
        val currentSelected = _selectedGuardians.value.toMutableSet()
        if (currentSelected.contains(contact)) {
            currentSelected.remove(contact)
        } else {
            currentSelected.add(contact)
        }
        _selectedGuardians.value = currentSelected
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

                // ✅ 보호자 ID 없이 세션을 시작 (빈 리스트 전달)
                val guardianIds = emptyList<Long>()
                
                // 예상 도착 시간 계산
                val expectedArrival: LocalDateTime? = when (_arrivalMode.value) {
                    ArrivalMode.TIMER -> {
                        // 현재 시간 + 타이머 분
                        LocalDateTime.now().plusMinutes(_timerMinutes.value.toLong())
                    }
                    ArrivalMode.SCHEDULED_TIME -> {
                        // 선택된 시간을 오늘 날짜와 결합
                        _expectedArrivalTime.value?.let { time ->
                            LocalDateTime.now().withHour(time.hour).withMinute(time.minute)
                        }
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
                val success = safeWalkRepository.endSafeWalkSession(currentSessionId, SessionEndReason.MANUAL)
                if (success) {
                    _isSessionActive.value = false
                    _sessionId.value = null
                    _escortFlowState.value = EscortFlowState.SETUP // 초기 화면으로 복귀

                    // 위치 추적 중지
                    locationTracker.stopLocationUpdates()
                } else {
                    // TODO: 종료 실패 처리
                    Log.d("EscortViewModel", "안심귀가 종료 실패")
                }
            }
        }
    }

    // ✅ 위치 트랙 업데이트 함수
    fun updateLocationTrack(lat: Double, lon: Double, accuracy: Double) {
        _sessionId.value?.let { sessionId ->
            viewModelScope.launch {
                try {
                    val success = safeWalkRepository.uploadLocationTrack(sessionId, lat, lon, accuracy)
                    if (!success) {
                        Log.w("EscortViewModel", "위치 트랙 업데이트 실패")
                    }
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
        _destinationLocation.value = LocationState(name, latLng)
    }

    // ✅ 보호자 공유 UI 토글 함수
    fun toggleGuardianShareSheet() {
        _showGuardianShareSheet.value = !_showGuardianShareSheet.value
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
                    val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (it.moveToNext()) {
                        val contactId = it.getLong(idIndex)
                        val name = it.getString(nameIndex) ?: "Unknown"
                        val number = it.getString(numberIndex) ?: ""
                        if (name.isNotEmpty() && number.isNotEmpty()) {
                            contactsList.add(Contact(contactId, name, number.replace("-", "").trim()))
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

    override fun onCleared() {
        super.onCleared()
        // ViewModel이 정리될 때 위치 추적 중지
        locationTracker.stopLocationUpdates()
    }

    // --- ✅ 주소 검색 관련 함수 추가 ---

    // '직접 입력' 버튼 클릭 시 호출
    fun startAddressSearch() {
        _isSearchingAddress.value = true
    }

    // 주소 검색창의 텍스트가 변경될 때 호출
    fun onSearchQueryChanged(query: String) {
        _addressSearchQuery.value = query
        // 간단한 디바운싱 로직 (실제로는 .debounce() 사용 권장)
        viewModelScope.launch {
            if (query.length > 1) {
                try {
                    _addressSearchResults.value = addressRepository.searchAddress(query)
                } catch (e: Exception) {
                    _addressSearchResults.value = emptyList()
                }
            } else {
                _addressSearchResults.value = emptyList()
            }
        }
    }

    // 검색 결과 목록에서 특정 주소를 선택했을 때 호출
    fun selectAddressForConfirmation(address: AddressModel) {
        _selectedAddressForConfirmation.value = address
    }

    // 지도 확인 화면에서 '도착지 설정' 버튼을 눌렀을 때 호출
    fun confirmDestination() {
        _selectedAddressForConfirmation.value?.let { address ->
            val latLng = LatLng(address.y.toDouble(), address.x.toDouble())
            val addressName = address.roadAddress.ifEmpty { address.jibunAddress }
            updateDestinationLocation(addressName, latLng)
            _isDestinationSelected.value = true
        }
        cancelAddressSearch() // 주소 검색 플로우 종료
    }

    // 주소 검색 취소 또는 완료 시 호출
    fun cancelAddressSearch() {
        _isSearchingAddress.value = false
        _addressSearchQuery.value = ""
        _addressSearchResults.value = emptyList()
        _selectedAddressForConfirmation.value = null
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