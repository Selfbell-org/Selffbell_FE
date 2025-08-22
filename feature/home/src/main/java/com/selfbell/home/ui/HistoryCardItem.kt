package com.selfbell.home.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCardItem(
    historyItem: SafeWalkHistoryItem, // SafeWalkDetail 타입
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ✅ 프로필 이미지 및 텍스트 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 프로필 이미지
                Image(
                    painter = painterResource(id = com.selfbell.core.R.drawable.default_profile_icon2),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                // 이름 및 주소/날짜
                Column {
                    Text(
                        text = historyItem.ward.name, // 실제 사용자 이름
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${historyItem.destination.addressText} · ${historyItem.startedAt.toLocalDate()}", // 실제 목적지와 시작 시간
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // ✅ 상태 텍스트
            Text(
                text = when (historyItem.status) {
                    "IN_PROGRESS" -> "귀가중"
                    "COMPLETED" -> "완료"
                    "CANCELED" -> "취소"
                    "ENDED" -> "완료"
                    else -> historyItem.status
                },
                style = Typography.bodyMedium,
                color = if (historyItem.status == "IN_PROGRESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}