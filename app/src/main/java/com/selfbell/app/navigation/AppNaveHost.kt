package com.selfbell.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding // 내비게이션 바 영역 패딩 추가
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // Alignment 임포트
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellBottomNavigation
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.feature.home.ui.HomeScreen // HomeScreen 임포트 (수정)

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    SelfBellTheme {
        // Box를 사용하여 화면 콘텐츠와 바텀바를 겹쳐 배치
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = modifier.fillMaxSize(), // Scaffold가 전체 화면을 차지하도록
                // bottomBar 슬롯은 사용하지 않거나 비워둡니다.
                // bottomBar = { /* 비워둠 */ }
            ) { paddingValues ->
                // NavHost는 바텀바와 겹치지 않도록 아래쪽에 충분한 패딩을 줍니다.
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.HOME_ROUTE,
                    // NavHost에 바텀바 높이 + 아래쪽 마진을 고려한 패딩을 줍니다.
                    // 이 paddingValues는 시스템 바(상단/하단)에 의해 자동으로 주어지는 패딩입니다.
                    // 바텀바가 겹치는 영역을 피하기 위해 하단 패딩을 추가합니다.
                    modifier = Modifier.padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 80.dp // 바텀바 높이 80dp + 아래쪽 추가 마진 16dp 정도
                    )
                ) {
                    composable(AppRoute.HOME_ROUTE) { HomeScreen() } // <-- 여기를 Text("홈 화면") 대신 HomeScreen()으로 변경
                    composable(AppRoute.ALERTS_ROUTE) { Text(text = "알림 화면") }
                    composable(AppRoute.ESCORT_ROUTE) { Text(text = "동행 화면") }
                    composable(AppRoute.SETTINGS_ROUTE) { Text(text = "설정 화면") }
                }
            }

            // 바텀 내비게이션 바를 Box의 하단 중앙에 배치
            // navigationBarsPadding()을 사용하여 시스템 내비게이션 바 영역 위에 패딩
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Box의 하단 중앙에 정렬
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // 스크린샷의 바텀바 아래쪽 공백
                    .navigationBarsPadding() // 시스템 내비게이션 바 영역에 대한 패딩
                    ,horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .padding(horizontal = 8.dp) // 스크린샷의 좌우 공백
                        .clip(RoundedCornerShape(40.dp)), // 스크린샷의 둥근 모서리
                    color = Color.White, // 바텀바 배경색 (Figma TABBAR 배경색)
                    shadowElevation = 8.dp // 그림자 효과 (Figma 디자인에 따라 조절)
                ) {
                    SelfBellBottomNavigation(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost(navController = rememberNavController())
}