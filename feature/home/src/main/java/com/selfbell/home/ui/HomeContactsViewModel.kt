package com.selfbell.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfbell.core.model.Contact
import com.selfbell.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _guardians = MutableStateFlow<List<Contact>>(emptyList())
    val guardians: StateFlow<List<Contact>> = _guardians.asStateFlow()

    init {
        loadAcceptedFriends()
    }

    private fun loadAcceptedFriends() {
        viewModelScope.launch {
            runCatching {
                contactRepository.getContactsFromServer(status = "ACCEPTED", page = 0, size = 100)
            }.onSuccess { friends ->
                val mapped = friends.map { rel ->
                    val phone = if (rel.toPhoneNumber.isNotBlank()) rel.toPhoneNumber else rel.fromPhoneNumber
                    val name = phone // TODO: 서버에서 이름 제공 시 교체
                    Contact(
                        id = rel.id.toLongOrNull() ?: 0L,
                        name = name,
                        phoneNumber = phone
                    )
                }
                _guardians.value = mapped
            }.onFailure {
                _guardians.value = emptyList()
            }
        }
    }
}


