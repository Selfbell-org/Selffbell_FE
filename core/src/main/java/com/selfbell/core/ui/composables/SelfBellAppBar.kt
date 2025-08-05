// core/src/main/java/com/selfbell/core/ui/composables/SelfBellAppBar.kt
package com.selfbell.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape // CircleShape 임포트
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black // Black 색상 임포트

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfBellAppBar(
    modifier: Modifier = Modifier,
    // title은 두 줄 텍스트를 담을 수 있는 별도 Composable로 정의
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
            containerColor = Color.Transparent, // 시안처럼 투명한 배경
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            actionIconContentColor = Black, // 아이콘 색상
            navigationIconContentColor = Black,
            titleContentColor = Black // 텍스트 색상
        )
    )
}

// 홈 화면용 GreetingSection을 titleContent로 전달하기 위한 Composable
@Composable
fun AppBarTwoLineTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = title, style = Typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = subtitle, style = Typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// 동행 친구 아이콘 (흰색 원 배경) Composable
@Composable
fun AppBarCircleIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(36.dp) // 원의 크기 (조절 가능)
                .clip(CircleShape) // 원형으로 자르기
                .background(Color.White), // 흰색 배경
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp) // 아이콘 크기 (조절 가능)
            )
        }
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
            navigationIcon = {
                IconButton(onClick = { /* Handle back click */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            },
            actions = {
                AppBarCircleIcon(
                    icon = Icons.Default.AccountBox,
                    contentDescription = "동행 친구",
                    onClick = { /* Handle 동행 친구 click */ }
                )
            }
        )
    }
}