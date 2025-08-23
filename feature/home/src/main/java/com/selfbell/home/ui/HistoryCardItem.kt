package com.selfbell.home.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkHistoryItem
import com.selfbell.domain.model.SafeWalkStatus
import com.selfbell.feature.home.R
import java.time.LocalDateTime
import com.selfbell.core.R as CoreR
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Success
import com.selfbell.core.ui.theme.Danger
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.composables.SelfBellButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCardItem(
    historyItem: SafeWalkHistoryItem, // SafeWalkDetail 타입
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = CoreR.drawable.default_profile_icon2),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = historyItem.wardName, // 실제 사용자 이름
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${historyItem.destinationName} · ${historyItem.startedAt.toLocalDate()}", // 실제 목적지와 시작 시간
                        style = Typography.bodySmall,
                        color = Black
                    )
                }
            }
            SelfBellStatusBadge(status = historyItem.status)
        }
    }
}

@Composable
private fun SelfBellStatusBadge(status: SafeWalkStatus) {
    val buttonType = when (status) {
        SafeWalkStatus.IN_PROGRESS -> SelfBellButtonType.PRIMARY_FILLED
        SafeWalkStatus.ARRIVED, SafeWalkStatus.MANUAL_END -> SelfBellButtonType.OUTLINED
        SafeWalkStatus.TIMEOUT -> SelfBellButtonType.OUTLINED // 취소 상태도 OUTLINED로 처리
    }

    val text = when (status) {
        SafeWalkStatus.IN_PROGRESS -> "귀가중"
        SafeWalkStatus.ARRIVED, SafeWalkStatus.MANUAL_END -> "완료"
        SafeWalkStatus.TIMEOUT -> "완료" // 취소 상태도 "완료"로 표시
    }

    SelfBellButton(
        text = text,
        onClick = { /* 상태 배지는 클릭 불가 */ },
        buttonType = buttonType,
        isSmall = true,
        enabled = true,
        modifier = Modifier.wrapContentSize()
    )
}