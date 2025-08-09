// auth/ui/ContactRegistrationViewModel.kt
package com.example.auth.ui

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.auth.ui.ProfileRegisterScreen

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String
)

@HiltViewModel
class ContactRegistrationViewModel @Inject constructor(
    private val contentResolver: ContentResolver // ContentResolver 주입
) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contactsList = mutableListOf<Contact>()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
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

                    // 중복 방지를 위해 Set을 사용할 수도 있습니다.
                    if (name.isNotEmpty() && number.isNotEmpty()) {
                        contactsList.add(Contact(contactId, name, number.replace("-", "").trim()))
                    }
                }
            }
            _allContacts.value = contactsList.distinctBy { it.phoneNumber } // 전화번호 중복 제거
        }
    }
}