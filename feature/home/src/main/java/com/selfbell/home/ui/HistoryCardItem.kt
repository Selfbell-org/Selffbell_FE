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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkHistoryItem
import com.selfbell.domain.model.SafeWalkStatus
import com.selfbell.feature.home.R
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCardItem(
    historyItem: SafeWalkHistoryItem,
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
                    painter = painterResource(id = com.selfbell.core.R.drawable.default_profile_icon2), // ✅ 기본 이미지 사용
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
                        text = historyItem.userName,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${historyItem.destinationName} · ${historyItem.dateTime.toLocalDate()}",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // ✅ 상태 텍스트
            Text(
                text = when (historyItem.status) {
                    SafeWalkStatus.IN_PROGRESS -> "귀가중"
                    SafeWalkStatus.COMPLETED -> "완료"
                    SafeWalkStatus.CANCELED -> "취소"
                    SafeWalkStatus.ENDED -> "완료" // ✅ ENDED 상태 추가
                },
                style = Typography.bodyMedium,
                color = if (historyItem.status == SafeWalkStatus.IN_PROGRESS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryCardItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            HistoryCardItem(
                historyItem = SafeWalkHistoryItem(
                    id = 1,
                    userProfileUrl = null,
                    userName = "엄마",
                    userType = "WARD",
                    destinationName = "우리집",
                    dateTime = LocalDateTime.now(),
                    status = SafeWalkStatus.IN_PROGRESS
                ),
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            HistoryCardItem(
                historyItem = SafeWalkHistoryItem(
                    id = 2,
                    userProfileUrl = null,
                    userName = "나의 귀가",
                    userType = "MINE",
                    destinationName = "회사",
                    dateTime = LocalDateTime.now().minusDays(2),
                    status = SafeWalkStatus.COMPLETED
                ),
                onClick = {}
            )
        }
    }
}