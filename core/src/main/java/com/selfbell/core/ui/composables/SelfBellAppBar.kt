// core/src/main/java/com/selfbell/core/ui/composables/SelfBellAppBar.kt
package com.selfbell.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfBellAppBar(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = titleContent,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            actionIconContentColor = Black,
            navigationIconContentColor = Black,
            titleContentColor = Black
        )
    )
}

// 제목 두 줄 처리를 위한 Composable
@Composable
fun AppBarTwoLineTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = title, style = Typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = subtitle, style = Typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Figma 스펙에 맞춰 수정된 AppBarCircleIcon Composable
@Composable
fun AppBarCircleIcon(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    // Figma Dev Mode에서 가져온 Modifier 스펙을 그대로 적용 [cite: image_6733ce.png, image_6733ea.png]
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                spotColor = Color(0x1A000000), // Figma에서 가져온 그림자 색상
                ambientColor = Color(0x1A000000) // Figma에서 가져온 그림자 색상
            )
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF), // Figma에서 가져온 테두리 색상
                shape = RoundedCornerShape(size = 99.dp) // Figma에서 가져온 모서리 둥글기
            )
            .size(48.dp) // Figma에서 가져온 크기
            .background(
                color = Color(0x80FFFFFF), // Figma에서 가져온 배경 색상
                shape = RoundedCornerShape(size = 99.dp) // 배경도 동일하게 둥글게
            )
            .clip(CircleShape) // 원형으로 자르기 (RoundedCornerShape(99.dp)와 유사)
            .padding(8.dp) // Figma에서 가져온 내부 패딩
    ) {
        Icon(
            painter = painterResource(id = iconResId), // <-- painterResource를 사용하여 리소스 ID 참조
            contentDescription = contentDescription,
            // 내부 아이콘 크기는 패딩을 제외한 남은 공간을 차지하도록 설정
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelfBellAppBarPreview() {
    SelfBellTheme {
        SelfBellAppBar(
            titleContent = {
                AppBarTwoLineTitle(title = "반갑습니다.", subtitle = "오늘도 안전한 하루 되세요.")
            },
            actions = {
                AppBarCircleIcon(
                    iconResId = R.drawable.ic_fellow, // <-- R.drawable로 변경한 예시
                    contentDescription = "동행 친구",
                    onClick = { /* Handle 동행 친구 click */ }
                )
            }
        )
    }
}