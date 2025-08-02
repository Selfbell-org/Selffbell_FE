package com.selfbell.core.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography // Typography 임포트
import com.selfbell.core.ui.theme.Primary // Primary 색상 임포트
import com.selfbell.core.ui.theme.Black // Black 색상 임포트
import com.selfbell.core.ui.theme.GrayInactivte // GrayInactive 색상 임포트

// Figma에 있는 버튼 타입 (예: Btn_Small, Btn_Large, FAB의 기본 스타일)을 Enum으로 정의
enum class SelfBellButtonType {
    PRIMARY_FILLED,   // 기본 채워진 버튼 (파란색 배경)
    OUTLINED,         // 외곽선 버튼
    TEXT_ONLY,        // 텍스트만 있는 버튼
    FAB               // FAB (하단 112 신고 버튼)
}

@Composable
fun SelfBellButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: SelfBellButtonType = SelfBellButtonType.PRIMARY_FILLED,
    enabled: Boolean = true
) {
    val backgroundColor: Color
    val contentColor: Color
    val borderColor: Color?
    val buttonHeight: Int = 56 // Figma의 Large 버튼 높이 (예시)
    val cornerRadius: Int = 8 // Figma의 Corner radius 8px

    when (buttonType) {
        SelfBellButtonType.PRIMARY_FILLED -> {
            backgroundColor = Primary
            contentColor = Color.White
            borderColor = null
        }
        SelfBellButtonType.OUTLINED -> {
            backgroundColor = Color.Transparent
            contentColor = Black // Figma에서 외곽선 버튼 텍스트 색상 확인
            borderColor = Black // Figma에서 외곽선 색상 확인
        }
        SelfBellButtonType.TEXT_ONLY -> {
            backgroundColor = Color.Transparent
            contentColor = Primary
            borderColor = null
        }
        SelfBellButtonType.FAB -> { // FAB 스타일 (예: 112 신고 버튼)
            backgroundColor = Primary
            contentColor = Color.White
            borderColor = null
            // FAB는 보통 텍스트 스타일이 다를 수 있지만, 여기서는 통일
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth() // 기본적으로 가로 전체 채우기
            .height(buttonHeight.dp), // Figma에서 확인한 높이 적용
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = GrayInactivte, // 비활성화 시 색상 (Figma의 GrayInactive 사용)
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(cornerRadius.dp), // 모서리 둥글게
        border = borderColor?.let { BorderStroke(1.dp, it) }, // 외곽선 버튼에만 적용
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp) // Figma에서 확인한 내부 패딩 (예시)
    ) {
        Text(
            text = text,
            style = Typography.titleMedium, // Figma Title, Semibold, 18 폰트 스타일 사용
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelfBellButtonPreview() {
    SelfBellTheme {
        Column(modifier = Modifier.padding(16.dp)) { // 미리보기에서 여러 버튼을 나란히 보기 위해 Column 사용
            SelfBellButton(text = "Primary 버튼", onClick = { /* TODO */ }, buttonType = SelfBellButtonType.PRIMARY_FILLED)
            Spacer(Modifier.height(8.dp))
            SelfBellButton(text = "Outlined 버튼", onClick = { /* TODO */ }, buttonType = SelfBellButtonType.OUTLINED)
            Spacer(Modifier.height(8.dp))
            SelfBellButton(text = "텍스트 버튼", onClick = { /* TODO */ }, buttonType = SelfBellButtonType.TEXT_ONLY)
            Spacer(Modifier.height(8.dp))
            SelfBellButton(text = "FAB 버튼 (112 신고)", onClick = { /* TODO */ }, buttonType = SelfBellButtonType.FAB)
            Spacer(Modifier.height(8.dp))
            SelfBellButton(text = "비활성화 버튼", onClick = { /* TODO */ }, enabled = false)
        }
    }
}