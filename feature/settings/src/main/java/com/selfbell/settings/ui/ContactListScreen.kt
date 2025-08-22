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

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        // ✅ 모든 디바이스 연락처를 노출
        items(deviceContacts) { contact ->
            val fallbackName = displayNameFromPhone(contact.phoneNumber, prefix = "연락처")
            val isExists = checkedContacts[contact.phoneNumber]

            // ✅ 상태에 따라 버튼 텍스트와 활성화 여부 결정
            val buttonText = when (isExists) {
                true -> "요청"
                false -> "초대"
                else -> "확인"
            }
            val isButtonEnabled = isExists != false

            ContactRegistrationListItem(
                name = contact.name.ifBlank { fallbackName },
                phoneNumber = contact.phoneNumber,
                buttonText = buttonText,
                isEnabled = isButtonEnabled,
                onButtonClick = {
                    when (isExists) {
                        true -> onSendRequest(contact.phoneNumber) // 가입자: 요청
                        false -> { /* TODO: 초대 로직 (SMS/딥링크) */
                        } // 미가입자: 초대
                        null -> { // 확인되지 않은 상태일 때만 서버에 요청
                            coroutineScope.launch {
                                // ✅ 콜백 함수를 전달하고, 콜백 내부에서 상태 업데이트
                                viewModel.checkUserExists(contact.phoneNumber) { exists ->
                                    checkedContacts =
                                        checkedContacts + (contact.phoneNumber to exists)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
    }