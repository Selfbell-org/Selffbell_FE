package com.selfbell.settings.ui

// feature/settings/ui/composables/FriendListItem.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfbell.domain.model.ContactRelationship
import com.selfbell.core.ui.theme.Typography

@Composable
fun FriendListItem(
    friend: ContactRelationship,
    onTogglePermission: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = friend.toPhoneNumber, style = Typography.titleMedium)
            Text(text = "친구 이름", style = Typography.bodyMedium) // TODO: 친구 이름 필드 추가
        }
        Switch(
            checked = friend.sharePermission, // TODO: ContactRelationship 모델에 sharePermission 필드 추가 필요
            onCheckedChange = { isChecked ->
                onTogglePermission(friend.id, isChecked)
            }
        )
    }
}