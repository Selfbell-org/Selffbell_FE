package com.selfbell.core.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfbell.domain.model.ContactRelationship

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

// 헬퍼 함수
private fun displayNameFromPhone(phone: String, prefix: String): String {
    return if (phone.isNotBlank()) {
        "$prefix $phone"
    } else {
        prefix
    }
}
