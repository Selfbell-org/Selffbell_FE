package com.selfbell.escort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography

@Composable
fun EscortingTopBar(
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit,
    onEndClick: () -> Unit // ✅ 종료 버튼 클릭 콜백 추가
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "안심 귀가 중입니다",
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )

        // ✅ 종료 버튼 추가
        SelfBellButton(
            text = "종료",
            onClick = onEndClick,
            isSmall = true,
            buttonType = SelfBellButtonType.OUTLINED
        )

        Spacer(modifier = Modifier.width(8.dp))

        SelfBellButton(
            text = "공유",
            onClick = onShareClick,
            isSmall = true
        )
    }
}