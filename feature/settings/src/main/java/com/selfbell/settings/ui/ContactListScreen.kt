// feature/settings/ui/ContactListScreen.kt
package com.selfbell.settings.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auth.ui.ContactUiState
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.composables.ContactRegistrationListItem // ✅ 재사용할 컴포넌트
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.composables.AcceptedFriendsList
import com.selfbell.core.ui.composables.UnregisteredContactItem
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactUser
import com.selfbell.settings.ui.ContactsUiState
import com.selfbell.settings.ui.ContactsViewModel
import kotlinx.coroutines.launch

// 탭 상태를 위한 enum
enum class ContactsTab {
    FRIENDS,
    REQUESTS,
    INVITE
}

// 전화번호 기반 더미 이름 생성 헬퍼
private fun displayNameFromPhone(phone: String, prefix: String): String {
    val trimmed = phone.replace(" ", "")
    val last4 = trimmed.takeLast(4)
    return "$prefix · ****$last4"
}

@Composable
fun ContactListScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(ContactsTab.FRIENDS) }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            ContactsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(text = when(tab) {
                            ContactsTab.FRIENDS -> "등록 친구"
                            ContactsTab.REQUESTS -> "요청"
                            ContactsTab.INVITE -> "친구 초대"
                        })
                    }
                )
            }
        }

        when (uiState) {
            is ContactsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ContactsUiState.Error -> {
                Text(text = (uiState as ContactsUiState.Error).message)
            }
            is ContactsUiState.Success -> {
                val successState = uiState as ContactsUiState.Success
                when (selectedTab) {
                    ContactsTab.FRIENDS -> {
                        AcceptedFriendsList(
                            friends = successState.acceptedFriends,
                            onTogglePermission = { contactId, allow ->
                                viewModel.toggleSharePermission(contactId, allow)
                            }
                        )
                    }
                    ContactsTab.REQUESTS -> {
                        PendingRequestsList(
                            pendingSent = successState.pendingSent,
                            pendingReceived = successState.pendingReceived,
                            onAcceptClick = { contactId ->
                                viewModel.acceptContactRequest(contactId)
                            }
                        )
                    }
                    ContactsTab.INVITE -> {
                        InviteFriendsList(
                            deviceContacts = successState.deviceContacts,
                            onSendRequest = { phoneNumber ->
                                viewModel.sendContactRequest(phoneNumber)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PendingRequestsList(
    pendingSent: List<ContactRelationship>,
    pendingReceived: List<ContactRelationship>,
    onAcceptClick: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("요청 목록", style = Typography.titleMedium)
        }
        items(pendingReceived, key = { it.id }) { request ->
            val phone = request.fromPhoneNumber.ifBlank { request.toPhoneNumber }
            val displayName = displayNameFromPhone(phone, prefix = "받은 요청")
            ContactRegistrationListItem(
                name = displayName,
                phoneNumber = phone,
                buttonText = "수락",
                isEnabled = true,
                onButtonClick = { onAcceptClick(request.id.toLongOrNull() ?: return@ContactRegistrationListItem) }
            )
            Divider()
        }
    }
}

@Composable
fun InviteFriendsList(
    deviceContacts: List<ContactUser>,
    onSendRequest: (String) -> Unit
) {
    // 📌 서버 가입 여부 상태를 저장하는 맵
    var checkedContacts by remember { mutableStateOf(mapOf<String, Boolean>()) }
    val coroutineScope = rememberCoroutineScope()
    val viewModel: ContactsViewModel = hiltViewModel()

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(searchQuery, deviceContacts) {
        if (searchQuery.isBlank()) deviceContacts
        else deviceContacts.filter { c ->
            c.name.contains(searchQuery, ignoreCase = true) ||
            c.phoneNumber.contains(searchQuery)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("\uc5f0\ub77d\ucc98 \uac80\uc0c9") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            // \u2705 \ubaa8\ub4e0 \ub514\ubc14\uc774\uc2a4 \uc5f0\ub77d\ucc98\ub97c \ub178\ucd9c (\uac80\uc0c9 \uc801\uc6a9)
            items(filteredContacts) { contact ->
                val fallbackName = displayNameFromPhone(contact.phoneNumber, prefix = "\uc5f0\ub77d\ucc98")
                val isExists = checkedContacts[contact.phoneNumber]

                // \u2705 \uc0c1\ud0dc\uc5d0 \ub530\ub77c \ubc84\ud2bc \ud14d\uc2a4\ud2b8/\ud65c\uc131\ud654 \uacb0\uc815
                val buttonText = when (isExists) {
                    true -> "\uc694\uccad"
                    false -> "\ucd08\ub300"
                    else -> "\ud655\uc778"
                }
                val isButtonEnabled = when (isExists) {
                    true -> false // already registered -> disable
                    false -> true // unregistered -> allow invite
                    null -> true // unknown -> allow check
                }

                // Derive status label inline without touching shared composable
                val statusLabel: (@Composable () -> Unit)? = when (isExists) {
                    true -> {
                        { Text(text = "\uc774\ubbf8 \ub4f1\ub85d\ub41c \uac00\uc785\uc790\uc785\ub2c8\ub2e4", style = Typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                    }
                    false -> {
                        { Text(text = "\uc11c\ubc84\uc5d0 \ub4f1\ub85d\ub418\uc9c0 \uc54a\uc740 \uc0ac\uc6a9\uc790", style = Typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                    }
                    else -> null
                }

                Column {
                    ContactRegistrationListItem(
                        name = contact.name.ifBlank { fallbackName },
                        phoneNumber = contact.phoneNumber,
                        buttonText = buttonText,
                        isEnabled = isButtonEnabled,
                        onButtonClick = {
                            when (isExists) {
                                true -> { /* disabled */ }
                                false -> { /* TODO: \ucd08\ub300 \ub85c\uc9c1 (SMS/\ub515\ub9c1\ud06c) */ }
                                null -> {
                                    coroutineScope.launch {
                                        viewModel.checkUserExists(contact.phoneNumber) { exists ->
                                            checkedContacts = checkedContacts + (contact.phoneNumber to exists)
                                        }
                                    }
                                }
                            }
                        }
                    )
                    if (statusLabel != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        statusLabel()
                    }
                    Divider()
                }
            }
        }
    }
}