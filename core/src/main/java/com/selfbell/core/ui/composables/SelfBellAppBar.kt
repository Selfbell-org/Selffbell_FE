package com.selfbell.core.ui.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfBellAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = Typography.headlineMedium, // 디자인에 따라 titleMedium으로 변경 가능
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions
    )
}

@Preview(showBackground = true)
@Composable
fun SelfBellAppBarPreview() {
    SelfBellTheme {
        SelfBellAppBar(
            title = "오늘의 알림",
            navigationIcon = {
                IconButton(onClick = { /* 뒤로가기 */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* 알림 클릭 */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "알림"
                    )
                }
            }
        )
    }
}