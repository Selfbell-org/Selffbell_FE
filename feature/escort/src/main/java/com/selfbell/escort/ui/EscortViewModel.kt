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
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.selfbell.core.model.Contact // 이 import를 사용합니다.
import com.selfbell.domain.model.SessionEndReason
import com.selfbell.domain.repository.SafeWalkRepository


// 연락처 데이터 클래스
data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String
)

@HiltViewModel
class EscortViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val safeWalkRepository: SafeWalkRepository
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

    // 연락처 관련 상태 추가
    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // ✅ 세션 관리 상태
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()
    private val _sessionId = MutableStateFlow<Long?>(null)

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
            }
        }
    }

    // ✅ 안심귀가 시작 함수
    fun startSafeWalk() {
        viewModelScope.launch {
            try {
                val session = safeWalkRepository.createSafeWalkSession(
                    originLat = _startLocation.value.latLng.latitude,
                    originLon = _startLocation.value.latLng.longitude,
                    originAddress = _startLocation.value.name,
                    destinationLat = _destinationLocation.value.latLng.latitude,
                    destinationLon = _destinationLocation.value.latLng.longitude,
                    destinationAddress = _destinationLocation.value.name,
                    expectedArrival = null, // TODO: 시간 지정 모드일 때 값 설정
                    timerMinutes = _timerMinutes.value,
                    guardianIds = emptyList() // TODO: 보호자 선택 기능 연동
                )
                // 성공 시 상태 업데이트
                _sessionId.value = session.sessionId
                _isSessionActive.value = true
            } catch (e: Exception) {
                Log.e("EscortViewModel", "세션 생성 실패", e)
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
                } else {
                    // TODO: 종료 실패 처리
                    Log.d("EscortViewModel", "안심귀가 종료 실패")
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
        }
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