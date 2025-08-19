// feature/escort/ui/EscortViewModel.kt
package com.selfbell.escort.ui

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
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
import java.time.LocalDateTime
import java.time.LocalTime
import retrofit2.HttpException

@HiltViewModel
class EscortViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val safeWalkRepository: SafeWalkRepository,
    private val locationTrackingService: LocationTrackingService,
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

    init {
        loadContacts()
        checkCurrentSession() // ✅ ViewModel 생성 시 진행 중인 세션 확인
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
                locationTrackingService.getLocationUpdates().collectLatest { location ->
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

    // ✅ 예상 도착 시간 설정 함수
    fun setExpectedArrivalTime(time: LocalTime) {
        _expectedArrivalTime.value = time
    }

    // ✅ 선택된 보호자들로 안심귀가 시작 함수
    fun startSafeWalkWithGuardians() {
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
                
                val guardianIds = _selectedGuardians.value.map { it.id }
                
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
                // TODO: 사용자에게 오류 알림
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
                    // 위치 추적 중지
                    locationTrackingService.stopLocationUpdates()
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

    fun updateStartLocation(name: String, latLng: LatLng) {
        _startLocation.value = LocationState(name, latLng)
    }
    fun updateDestinationLocation(name: String, latLng: LatLng) {
        _destinationLocation.value = LocationState(name, latLng)
    }
    fun setArrivalMode(mode: ArrivalMode) {
        _arrivalMode.value = mode
    }
    fun setTimerMinutes(minutes: Int) {
        _timerMinutes.value = minutes
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
        locationTrackingService.stopLocationUpdates()
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