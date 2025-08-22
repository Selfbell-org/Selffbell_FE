package com.selfbell.settings.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactRelationshipStatus
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

sealed interface ContactsUiState {
    object Loading : ContactsUiState
    data class Success(
        val acceptedFriends: List<ContactRelationship>,
        val pendingSent: List<ContactRelationship>,
        val pendingReceived: List<ContactRelationship>,
        val deviceContacts: List<ContactUser>
    ) : ContactsUiState
    data class Error(val message: String) : ContactsUiState
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadAllContactData()
    }

    fun loadAllContactData() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            try {
                val accepted = contactRepository.getContactsFromServer(status = "ACCEPTED", page = 0, size = 50)
                val pendingAll = contactRepository.getContactsFromServer(status = "PENDING", page = 0, size = 50)
                // 서버 응답에 발신/수신 구분 정보가 부족하여 일단 전부 받은 요청으로 분류
                val pendingSent = emptyList<ContactRelationship>()
                val pendingReceived = pendingAll
                // 서버 존재 여부까지 포함해서 디바이스 연락처 로드
                val deviceContacts = contactRepository.getDeviceContacts()
                _uiState.value = ContactsUiState.Success(accepted, pendingSent, pendingReceived, deviceContacts)
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error(e.message ?: "데이터 로드 실패")
            }
        }
    }

    // 친구 요청 수락 로직
    fun acceptContactRequest(contactId: Long) {
        viewModelScope.launch {
            try {
                contactRepository.acceptContactRequest(contactId)
                loadAllContactData()
            } catch (e: Exception) {
                // 에러 처리 (토스트/스낵바 등)
            }
        }
    }

    // 위치 공유 권한 변경 로직 (서버 API 준비 후 연결)
    fun toggleSharePermission(contactId: String, allow: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: 서버 API 연동
                loadAllContactData()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 친구 요청 보내기
    fun sendContactRequest(toPhoneNumber: String) {
        viewModelScope.launch {
            try {
                contactRepository.sendContactRequest(toPhoneNumber)
                loadAllContactData()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun checkUserExists(phoneNumber: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val exists = contactRepository.checkUserExists(phoneNumber)
                onResult(exists)
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun inviteContact(phoneNumber: String) {
        // TODO: 초대(SMS/딥링크) 연동
    }
}