package com.selfbell.settings.ui

// feature/settings/ui/composables/RequestListItem.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.domain.model.ContactRelationship

@Composable
fun RequestListItem(
    request: ContactRelationship,
    onAcceptClick: (Long) -> Unit,
    isSent: Boolean // 내가 보낸 요청인지, 받은 요청인지 구분
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = if (isSent) request.toPhoneNumber else request.fromPhoneNumber)
        if (!isSent) {
            SelfBellButton(
                text = "수락",
                onClick = { /* onAcceptClick(request.id) */ }, // Long 타입 ID 필요
                isSmall = true
            )
        } else {
            Text(text = "수락 대기 중")
        }
    }
}