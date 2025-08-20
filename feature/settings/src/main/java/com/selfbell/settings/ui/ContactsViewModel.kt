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
            delay(1000) // Simulate network delay

            try {
                // --- Mock Data for Testing ---
                // In a real app, you would call the repository here.
                val accepted = listOf(
                    ContactRelationship(
                        id = "rel_1",
                        fromUserId = "me_id",
                        toUserId = "friend_1_id",
                        fromPhoneNumber = "010-1234-5678",
                        toPhoneNumber = "010-1111-2222",
                        status = ContactRelationshipStatus.ACCEPTED,
                        createdAt = "...", updatedAt = "...",
                        sharePermission = true
                    )
                )

                val pendingSent = listOf(
                    ContactRelationship(
                        id = "rel_2",
                        fromUserId = "me_id",
                        toUserId = "friend_2_id",
                        fromPhoneNumber = "010-1234-5678",
                        toPhoneNumber = "010-3333-4444",
                        status = ContactRelationshipStatus.PENDING,
                        createdAt = "...", updatedAt = "...",
                        sharePermission = true
                    )
                )

                val pendingReceived = listOf(
                    ContactRelationship(
                        id = "rel_3",
                        fromUserId = "friend_3_id",
                        toUserId = "me_id",
                        fromPhoneNumber = "010-5555-6666",
                        toPhoneNumber = "010-1234-5678",
                        status = ContactRelationshipStatus.PENDING,
                        createdAt = "...", updatedAt = "...",
                        sharePermission = true
                    )
                )

                val deviceContacts = listOf(
                    ContactUser(
                        id = "contact_1", name = "등록된 친구", phoneNumber = "010-1111-2222", isExists = true
                    ),
                    ContactUser(
                        id = "contact_2", name = "수락 대기중", phoneNumber = "010-3333-4444", isExists = true
                    ),
                    ContactUser(
                        id = "contact_3", name = "가입 안한 친구", phoneNumber = "010-7777-8888", isExists = false
                    ),
                    ContactUser(
                        id = "contact_4", name = "가입한 친구", phoneNumber = "010-9999-0000", isExists = true
                    )
                )

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
                // contactRepository.acceptContactRequest(contactId)
                // 성공적으로 수락한 후, 목록을 새로고침
                loadAllContactData()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 위치 공유 권한 변경 로직
    fun toggleSharePermission(contactId: String, allow: Boolean) {
        viewModelScope.launch {
            try {
                // contactRepository.toggleSharePermission(contactId, allow)
                // 성공적으로 변경한 후, 목록 새로고침
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
                // contactRepository.sendContactRequest(toPhoneNumber)
                // 요청 성공 후 목록 새로고침
                loadAllContactData()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}