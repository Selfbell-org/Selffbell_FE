package com.example.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.domain.model.ContactUser
import com.selfbell.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ContactUiState {
    object Idle : ContactUiState
    object Loading : ContactUiState
    data class Success(val contacts: List<ContactUser>) : ContactUiState
    data class Error(val message: String) : ContactUiState
}

@HiltViewModel
class ContactRegistrationViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactUiState>(ContactUiState.Idle)
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactUser>>(emptyList())
    val contacts: StateFlow<List<ContactUser>> = _contacts.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterContacts()
    }

    // 로컬 디바이스 연락처만 불러오기 (서버 체크 없이)
    fun loadDeviceContactsOnly() {
        _uiState.value = ContactUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deviceContacts = contactRepository.loadDeviceContactsWithUserCheck() // TODO: 로컬 전용으로 대체 필요 시 구현
                _contacts.value = deviceContacts
                _uiState.value = ContactUiState.Success(deviceContacts)
                Log.d("ContactRegistrationVM", "연락처 로드 완료: ${deviceContacts.size}개")
            } catch (e: Exception) {
                Log.e("ContactRegistrationVM", "연락처 로드 실패", e)
                _uiState.value = ContactUiState.Error(e.message ?: "연락처를 불러오는데 실패했습니다.")
            }
        }
    }

    fun checkUserExists(phoneNumber: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val exists = contactRepository.checkUserExists(phoneNumber)
                onResult(exists)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun inviteContact(phoneNumber: String) {
        // TODO: 초대 로직 (SMS/딥링크 등) 연결
        Log.d("ContactRegistrationVM", "초대 전송: $phoneNumber")
    }

    fun sendContactRequest(toPhoneNumber: String) {
        viewModelScope.launch {
            try {
                contactRepository.sendContactRequest(toPhoneNumber)
                Log.d("ContactRegistrationVM", "보호자 요청 성공: $toPhoneNumber")
                updateContactStatus(toPhoneNumber, true)
            } catch (e: Exception) {
                Log.e("ContactRegistrationVM", "보호자 요청 실패: $toPhoneNumber", e)
                _uiState.value = ContactUiState.Error(e.message ?: "요청 전송 중 오류가 발생했습니다.")
            }
        }
    }

    private fun filterContacts() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _uiState.value = ContactUiState.Success(_contacts.value)
        } else {
            val filteredContacts = _contacts.value.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumber.contains(query)
            }
            _uiState.value = ContactUiState.Success(filteredContacts)
        }
    }

    private fun updateContactStatus(phoneNumber: String, requestSent: Boolean) {
        val updatedContacts = _contacts.value.map { contact ->
            if (contact.phoneNumber == phoneNumber) {
                contact.copy(
                    relationshipStatus = if (requestSent) {
                        com.selfbell.domain.model.ContactRelationshipStatus.PENDING
                    } else {
                        com.selfbell.domain.model.ContactRelationshipStatus.NONE
                    }
                )
            } else {
                contact
            }
        }
        _contacts.value = updatedContacts
        filterContacts()
    }

    fun refreshContacts() {
        loadDeviceContactsOnly()
    }
}