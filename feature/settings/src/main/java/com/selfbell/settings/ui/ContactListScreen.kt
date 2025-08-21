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
import com.selfbell.core.ui.composables.ContactListItem
import com.selfbell.core.ui.composables.SelfBellButton // SelfBellButton 추가
import com.selfbell.core.ui.composables.SelfBellButtonType // SelfBellButtonType 추가
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.domain.model.ContactUser
import com.selfbell.settings.ui.ContactsUiState
import com.selfbell.settings.ui.ContactsViewModel

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

// ✅ 등록 친구 목록
@Composable
fun AcceptedFriendsList(
    friends: List<ContactRelationship>,
    onTogglePermission: (String, Boolean) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(friends, key = { it.id }) { friend ->
            val phone = friend.toPhoneNumber // 현재 더미에서는 상대방 번호로 표시
            val displayName = displayNameFromPhone(phone, prefix = "친구")
            ContactListItem(
                name = displayName,
                phoneNumber = phone,
                isSelected = false, // 초대/요청 스타일과 동일한 틀 유지
                isEnabled = false,  // 친구 목록은 버튼 비활성화 (추후 기능 연결 가능)
                onButtonClick = { /* 친구 항목: 별도 버튼 동작 없음 (추후 권한 토글 등 연결) */ }
            )
            Divider()
        }
    }
}

// ✅ 요청 목록
@Composable
fun PendingRequestsList(
    pendingSent: List<ContactRelationship>,
    pendingReceived: List<ContactRelationship>,
    onAcceptClick: (Long) -> Unit // contactId Long
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
//        item { Text("내가 보낸 요청", style = Typography.titleMedium) }
//        items(pendingSent, key = { it.id }) { request ->
//            val phone = request.toPhoneNumber
//            val displayName = displayNameFromPhone(phone, prefix = "보낸 요청")
//            // 보낸 요청: "수락 대기 중" → 버튼 비활성화 동일 UI 사용
//            ContactListItem(
//                name = displayName,
//                phoneNumber = phone,
//                isSelected = false,
//                isEnabled = false, // 비활성화
//                onButtonClick = { /* 비활성화 */ }
//            )
//            Divider()
//        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("요청 목록", style = Typography.titleMedium)
        }
        items(pendingReceived, key = { it.id }) { request ->
            val phone = request.fromPhoneNumber.ifBlank { request.toPhoneNumber }
            val displayName = displayNameFromPhone(phone, prefix = "받은 요청")
            // 받은 요청: '수락' 가능 → 같은 UI에서 활성 버튼처럼 동작
            ContactListItem(
                name = displayName,
                phoneNumber = phone,
                isSelected = true, // 선택 가능 느낌 유지
                isEnabled = true,
                onButtonClick = { onAcceptClick(request.id.toLongOrNull() ?: return@ContactListItem) }
            )
            Divider()
        }
    }
}

// ✅ 친구 초대 목록
@Composable
fun InviteFriendsList(
    deviceContacts: List<ContactUser>,
    onSendRequest: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(deviceContacts) { contact ->
            val fallbackName = displayNameFromPhone(contact.phoneNumber, prefix = "연락처")
            ContactListItem(
                name = contact.name.ifBlank { fallbackName },
                phoneNumber = contact.phoneNumber,
                isSelected = false,
                isEnabled = contact.isExists,
                onButtonClick = { onSendRequest(contact.phoneNumber) }
            )
        }
    }
}