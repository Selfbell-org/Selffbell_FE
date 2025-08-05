package com.selfbell.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row // Row 임포트
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.GrayInactive


data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun SelfBellBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(AppRoute.HOME_ROUTE, Icons.Default.Home, "홈"),
        BottomNavItem(AppRoute.ALERTS_ROUTE, Icons.Default.Info, "알림"),
        BottomNavItem(AppRoute.ESCORT_ROUTE, Icons.Default.AccountBox, "동행"),
        BottomNavItem(AppRoute.SETTINGS_ROUTE, Icons.Default.Settings, "설정"),
        BottomNavItem(AppRoute.FRIENDS_ROUTE, Icons.Default.Settings, "친구")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = modifier.height(80.dp), // 바텀바 자체의 높이 유지 ㅋ
        containerColor = Color.Transparent, // 바깥 Surface가 배경색을 담당
        contentColor = Black, // 기본 콘텐츠 색상 (아이템의 colors 파라미터가 오버라이드)
        tonalElevation = 0.dp // 그림자 제거
    ) {
        // NavigationBar 내부의 모든 아이템을 감싸는 Row에 horizontal padding 적용
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp), //  아이템들을 안쪽으로 밀어 넣음
            horizontalArrangement = Arrangement.SpaceAround, // 아이템 균등 배치
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 0.dp) // Column의 세로 패딩 조절
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(32.dp) // 아이콘 크기
                            )
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp) // 아이콘과 텍스트 사이 패딩
                            )
                        }
                    },
                    label = null, // Text 컴포넌트가 icon 슬롯 안으로 이동
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    alwaysShowLabel = true,
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = GrayInactive,
                        unselectedTextColor = GrayInactive
                    ),
                    modifier = Modifier.weight(1f) //  아이템이 공간을 균등하게 차지하도록 weight 적용
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelfBellBottomNavigationPreview() {
    SelfBellTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            NavigationBar(
                modifier = Modifier.height(80.dp),
                containerColor = Color.Transparent,
                contentColor = Black,
                tonalElevation = 0.dp
            ) {
                // 미리보기에서도 Row에 horizontal padding 적용
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        BottomNavItem(AppRoute.HOME_ROUTE, Icons.Default.Home, "홈"),
                        BottomNavItem(AppRoute.ALERTS_ROUTE, Icons.Default.Info, "알림"),
                        BottomNavItem(AppRoute.ESCORT_ROUTE, Icons.Default.AccountBox, "동행"),
                        BottomNavItem(AppRoute.SETTINGS_ROUTE, Icons.Default.Settings, "설정"),
                        BottomNavItem(AppRoute.FRIENDS_ROUTE, Icons.Default.Settings, "친구")
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = item.label,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            },
                            label = null,
                            selected = item.route == AppRoute.HOME_ROUTE,
                            onClick = {},
                            alwaysShowLabel = true,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = GrayInactive,
                                unselectedTextColor = GrayInactive
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}