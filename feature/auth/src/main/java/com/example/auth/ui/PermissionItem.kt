package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auth.R
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.White
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import com.selfbell.core.ui.theme.Gray50
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Third

// 권한 항목의 상태를 정의하는 Enum
enum class PermissionState {
    INACTIVE,   // 비활성화 상태
    ACTIVE,     // 현재 진행 중인 상태
    COMPLETED   // 완료된 상태
}

/**
 * 재활용 가능한 권한 항목 UI 컴포넌트입니다.
 * 상태에 따라 배경색, 텍스트 색상, 아이콘이 변경됩니다.
 *
 * @param title 항목의 제목 (예: "위치 권한")
 * @param description 항목의 설명
 * @param leftIconResId 왼쪽 아이콘 리소스 ID
 * @param state 현재 권한 요청의 상태 (INACTIVE, ACTIVE, COMPLETED)
 * @param onClick 항목 클릭 시 실행될 람다 함수
 */
@Composable
fun PermissionItem(
    title: String,
    description: String,
    leftIconResId: Int, // <-- 왼쪽 아이콘을 위한 파라미터 추가
    state: PermissionState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // 상태에 따라 컨테이너 색상 결정
    val containerColor = when (state) {
        PermissionState.INACTIVE -> Gray50
        PermissionState.ACTIVE, PermissionState.COMPLETED -> Third
    }
    // 상태에 따라 텍스트 및 아이콘 색상 결정
    val contentColor = when (state) {
        PermissionState.INACTIVE -> Black
        PermissionState.ACTIVE, PermissionState.COMPLETED -> Black
    }
    // 상태에 따라 오른쪽 아이콘 리소스 결정
    val rightIconResId = when (state) {
        PermissionState.COMPLETED -> R.drawable.check_icon
        else -> R.drawable.arrow_icon
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), // 아이템 간 간격 추가
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 아이콘 (원형 배경)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = leftIconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        // 텍스트 영역
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = Typography.bodyMedium,
                color = contentColor
            )
        }

        // 오른쪽 아이콘
        Image(
            painter = painterResource(id = rightIconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionItemPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        // 프리뷰에서는 R.drawable.ic_location 등 실제 리소스 ID를 사용
        PermissionItem(title = "위치 권한", description = "위치 주변 성범죄자 정보 제공", leftIconResId = R.drawable.mappinline_icon, state = PermissionState.INACTIVE, onClick = {})
        Spacer(Modifier.height(8.dp))
        PermissionItem(title = "위치 권한", description = "위치 주변 성범죄자 정보 제공", leftIconResId = R.drawable.bellringing_icon, state = PermissionState.ACTIVE, onClick = {})
        Spacer(Modifier.height(8.dp))
        PermissionItem(title = "위치 권한", description = "위치 주변 성범죄자 정보 제공", leftIconResId = R.drawable.addressbook_icon, state = PermissionState.COMPLETED, onClick = {})
    }
}
