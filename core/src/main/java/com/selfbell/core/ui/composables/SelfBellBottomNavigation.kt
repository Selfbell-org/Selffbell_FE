package com.selfbell.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.selfbell.core.ui.theme.GrayInactive

@Composable
fun SelfBellBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(AppRoute.HOME_ROUTE, R.drawable.nav_home_icon, "홈"),
        BottomNavItem(AppRoute.ESCORT_ROUTE, R.drawable.nav_location_icon, "동행"),
        BottomNavItem(AppRoute.HISTORY_ROUTE, R.drawable.nav_history_icon, "히스토리"),
        BottomNavItem(AppRoute.SETTINGS_ROUTE, R.drawable.nav_user_info_icon, "내 정보")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        // 👇 [수정] 요청하신 Modifier 속성을 적용합니다.
        modifier = modifier
            .shadow(elevation = 16.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
            .shadow(elevation = 8.dp, spotColor = Color(0x14000000), ambientColor = Color(0x14000000))
            .border(width = 1.dp, color = Color(0x4DFFFFFF), shape = RoundedCornerShape(size = 99.dp))
            .width(343.dp)
            .height(64.dp)
            .background(color = Color(0x80FFFFFF), shape = RoundedCornerShape(size = 99.dp))
            .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        containerColor = Color.Transparent,
        contentColor = Black,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconAlpha = if (selected) 1f else 0.2f

                NavigationBarItem(
                    icon = {
                        // 👇 [수정] 요청하신 Column 속성을 적용합니다.
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            val iconModifier = Modifier
                                .size(32.dp)
                                .alpha(iconAlpha)

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
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = GrayInactive,
                        unselectedTextColor = GrayInactive.copy(alpha = 0.5f)
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
        val navController = rememberNavController()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Preview를 위한 외부 패딩
            color = Color.Gray // 배경과 구분되도록 색상 변경
        ) {
            // Preview에서도 동일한 스타일이 적용되도록 modifier를 전달합니다.
            SelfBellBottomNavigation(
                navController = navController,
                // Preview에서는 modifier를 직접 설정하여 가운데 정렬
                modifier = Modifier
            )
        }
    }
}