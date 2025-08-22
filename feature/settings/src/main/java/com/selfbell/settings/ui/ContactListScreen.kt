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
import com.selfbell.core.ui.composables.AcceptedFriendsList
import com.selfbell.core.ui.composables.UnregisteredContactItem
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactUser
import com.selfbell.settings.ui.ContactsUiState
import com.selfbell.settings.ui.ContactsViewModel
import kotlinx.coroutines.launch
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.composables.ButtonState // ✅ ButtonState enum import

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

// ✅ 컴포넌트의 UI 데이터를 정의하는 data class
data class ContactItemUi(
    val buttonState: ButtonState,
    val buttonText: String,
    val isButtonEnabled: Boolean,
    val statusLabel: (@Composable () -> Unit)?
)

// ✅ ButtonState enum 클래스 재정의: 상태를 더 명확하게 구분합니다.
enum class ButtonState {
    REQUESTABLE, // 요청 가능 상태 (서버에 등록된 사용자)
    INVITABLE,   // 초대 가능 상태 (서버에 등록되지 않은 사용자)
    CHECKABLE,   // 확인이 필요한 초기 상태
    ACCEPTED,    // 이미 친구인 상태
    REMOVED,     // 제거된 상태
    DEFAULT      // 기본 상태 (수락 등)
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
                buttonState = ButtonState.DEFAULT, // "수락" 버튼은 기본 상태로 설정
                onButtonClick = { onAcceptClick(request.id.toLongOrNull() ?: return@ContactRegistrationListItem) }
            )
            Divider()
            Spacer(modifier = Modifier.height(90.dp)) // ✅ 바텀바 높이만큼의 공간 확보
        }
    }
}

@Composable
fun InviteFriendsList(
    deviceContacts: List<ContactUser>,
    onSendRequest: (String) -> Unit
) {
    var checkedContacts by remember { mutableStateOf(mapOf<String, Boolean>()) }
    val coroutineScope = rememberCoroutineScope()
    val viewModel: ContactsViewModel = hiltViewModel()

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
            placeholder = { Text("연락처 검색") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) { // ✅ weight(1f) 추가
            items(filteredContacts) { contact ->
                val fallbackName = displayNameFromPhone(contact.phoneNumber, prefix = "연락처")
                val isExists = checkedContacts[contact.phoneNumber]

                // ✅ isExists 상태에 따라 UI 데이터 생성
                val uiData = remember(isExists) {
                    when (isExists) {
                        true -> ContactItemUi(
                            ButtonState.INVITED,
                            "요청",
                            true,
                            @Composable { Text(text = "이미 등록된 가입자입니다", style = Typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                        )
                        false -> ContactItemUi(
                            ButtonState.DEFAULT,
                            "초대",
                            true,
                            @Composable { Text(text = "서버에 등록되지 않은 사용자", style = Typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                        )
                        else -> ContactItemUi(
                            ButtonState.DEFAULT,
                            "확인",
                            true,
                            null
                        )
                    }
                }

                Column {
                    ContactRegistrationListItem(
                        name = contact.name.ifBlank { fallbackName },
                        phoneNumber = contact.phoneNumber,
                        buttonText = uiData.buttonText,
                        isEnabled = uiData.isButtonEnabled,
                        buttonState = uiData.buttonState,
                        onButtonClick = {
                            coroutineScope.launch {
                                when (isExists) {
                                    true -> {
                                        viewModel.sendContactRequest(contact.phoneNumber)
                                    }
                                    false -> {
                                        onSendRequest(contact.phoneNumber)
                                    }
                                    null -> {
                                        viewModel.checkUserExists(contact.phoneNumber) { exists ->
                                            checkedContacts = checkedContacts + (contact.phoneNumber to exists)
                                        }
                                    }
                                }
                            }
                        }
                    )
                    if (uiData.statusLabel != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        uiData.statusLabel.invoke()
                    }
                    Divider()
                }
            }
        }
        Spacer(modifier = Modifier.height(90.dp)) // ✅ 바텀바 높이만큼의 공간 확보

    }
}