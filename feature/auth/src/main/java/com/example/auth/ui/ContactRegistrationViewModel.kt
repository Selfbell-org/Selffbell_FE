package com.example.auth.ui

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.core.model.Contact
import com.selfbell.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.auth.ui.ContactUiState // 📌 외부 파일에 있는 UiState를 import

@HiltViewModel
class ContactRegistrationViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactUiState>(ContactUiState.Idle)
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadAndSortContacts() {
        _uiState.value = ContactUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val localContacts = getLocalContacts()
                val tempToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
                val serverContacts = contactRepository.getContactsFromServer(
                    token = tempToken,
                    status = "PENDING",
                    page = 0,
                    size = 100
                )
                val serverPhoneNumbers = serverContacts.map { it.other.phoneNumber }.toSet()
                val sortedContacts = localContacts.sortedByDescending {
                    serverPhoneNumbers.contains(it.phoneNumber)
                }
                _uiState.value = ContactUiState.Success(sortedContacts)
            } catch (e: Exception) {
                Log.e("ContactRegistrationVM", "연락처 로드 및 정렬 실패", e)
                _uiState.value = ContactUiState.Error(e.message ?: "연락처를 불러오는데 실패했습니다.")
            }
        }
    }

    fun sendContactRequest(token: String, toPhoneNumber: String) {
        viewModelScope.launch {
            try {
                contactRepository.sendContactRequest(token, toPhoneNumber)
                Log.d("ContactRegistrationVM", "보호자 요청 성공: $toPhoneNumber")
            } catch (e: Exception) {
                Log.e("ContactRegistrationVM", "보호자 요청 실패: $toPhoneNumber", e)
                _uiState.value = ContactUiState.Error(e.message ?: "요청 전송 중 오류가 발생했습니다.")
            }
        }
    }

    private fun getLocalContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val idIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val contactId = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                if (name.isNotEmpty() && number.isNotEmpty()) {
                    contactsList.add(Contact(contactId, name, number.replace("-", "").trim()))
                }
            }
        }
        return contactsList.distinctBy { it.phoneNumber }
    }
}