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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.R
import com.selfbell.core.model.BottomNavItem
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.GrayInactive




@Composable
fun SelfBellBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        // 홈 아이콘을 core/drawable 리소스로 변경
        BottomNavItem(AppRoute.HOME_ROUTE, R.drawable.nav_home_icon, "홈"), // 예시: nav_home_icon.xml
        BottomNavItem(AppRoute.ESCORT_ROUTE, R.drawable.nav_location_icon, "동행" ),
        BottomNavItem(AppRoute.HISTORY_ROUTE, R.drawable.nav_history_icon, "히스토리"),
        BottomNavItem(AppRoute.SETTINGS_ROUTE, R.drawable.nav_user_info_icon, "내 정보")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = modifier.height(70.dp),
        containerColor = Color.Transparent,
        contentColor = Black,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconAlpha = if (selected) 1f else 0.2f // 선택 시 100%, 미선택 시 20%

                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 0.dp)
                        ) {
                            val iconModifier = Modifier
                                .size(32.dp)
                                .alpha(iconAlpha) // 아이콘에 alpha 적용

                            if (item.icon != null) {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.label,
                                    modifier = iconModifier
                                )
                            }
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                // 텍스트의 투명도는 colors에서 unselectedTextColor로 관리되므로
                                // 여기서는 별도 alpha 적용 안 함. 만약 아이콘과 동일하게 하고 싶다면 추가 가능
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    },
                    label = null,
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary, // 선택된 아이콘 색상 (alpha는 위에서 직접 제어)
                        selectedTextColor = Primary,
                        indicatorColor = Color.Transparent,
                        // unselectedIconColor의 alpha는 위에서 직접 제어하므로, 여기서는 기본 색상만 지정
                        unselectedIconColor = GrayInactive,
                        unselectedTextColor = GrayInactive.copy(alpha = 0.5f) // 예시: 미선택 텍스트도 약간 투명하게
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelfBellBottomNavigationPreview() {
    SelfBellTheme {
        // Preview용 NavController 추가
        val navController = rememberNavController()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            // SelfBellBottomNavigation에 navController 전달
            SelfBellBottomNavigation(navController = navController)
        }
    }
}