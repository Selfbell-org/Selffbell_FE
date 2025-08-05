// app/src/main/java/com/selfbell/app/navigation/AppNavHost.kt
package com.selfbell.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState // currentBackStackEntryAsState 임포트
import androidx.navigation.compose.rememberNavController
import com.example.auth.ui.LandingScreen
import com.example.auth.ui.LoginScreen
import com.example.auth.ui.SignUpScreen
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellBottomNavigation
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.app.ui.SplashScreen
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.feature.home.ui.HomeScreen // HomeScreen 임포트


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    SelfBellTheme {
        // 현재 라우트 상태를 가져와 바텀바 표시 여부 결정
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        // 바텀바를 표시하지 않을 라우트 목록
        val routesWithoutBottomBar = remember {
            setOf(
                AppRoute.SPLASH_ROUTE,
                AppRoute.LANDING_ROUTE,
                AppRoute.LOGIN_ROUTE, // LOGIN_PIN_ROUTE 또는 LOGIN_ROUTE (AppRoute 정의에 따라)
                AppRoute.SIGNUP_ROUTE
                // 필요하다면 다른 라우트도 추가
            )
        }
        val shouldShowBottomBar = currentRoute !in routesWithoutBottomBar
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.SPLASH_ROUTE,
                    modifier = Modifier.padding(
                        top = paddingValues.calculateTopPadding(),
                        // 스플래시 화면이 아닐 때만 바텀바 높이를 고려
                        bottom = if (shouldShowBottomBar) {
                            paddingValues.calculateBottomPadding() + 96.dp // 바텀바 높이만큼 패딩
                        } else {
                            paddingValues.calculateBottomPadding() // 또는 0.dp (시스템 네비게이션 바 패딩만 고려)
                        })
                ) {
                    // 스플래시 화면 라우트
                    composable(AppRoute.SPLASH_ROUTE) {
                        SplashScreen(navController = navController)
                    }

                    // 메인 탭 화면 라우트 (여기에 실제 feature 모듈의 화면이 연결될 것임)
                    composable(AppRoute.HOME_ROUTE) { HomeScreen() }
                    composable(AppRoute.ALERTS_ROUTE) { Text(text = "알림 화면") }
                    composable(AppRoute.ESCORT_ROUTE) { Text(text = "동행 화면") }
                    composable(AppRoute.SETTINGS_ROUTE) { Text(text = "설정 화면") }
                    composable(AppRoute.FRIENDS_ROUTE) { Text(text = "친구 화면") }
                    composable(AppRoute.LANDING_ROUTE) { LandingScreen(
                        onLoginClick = {navController.navigate(AppRoute.LOGIN_ROUTE)},
                        onSignUpClick = {navController.navigate(AppRoute.SIGNUP_ROUTE)}) }
                    composable(AppRoute.LOGIN_ROUTE) { /*LoginScreen(
                        onNavigateUp = {navController.popBackStack()},
                        onPinCompleted = {pin ->
                            println("입력된 PIN: $pin")
                    })*/ ReusableNaverMap()
                    }
                    composable(AppRoute.SIGNUP_ROUTE) {
                        var nickname by remember{ mutableStateOf("") }
                        SignUpScreen(nickname,
                            onNicknameChange = {newNickname
                                               -> nickname = newNickname},
                            onRegister = {nickname = it},
                            onNavigateUp = {navController.popBackStack()}) }
                }
            }

            // 바텀 내비게이션 바를 Box의 하단 중앙에 배치
            // 현재 라우트가 스플래시 화면이 아닐 때만 바텀바를 표시
            if (shouldShowBottomBar) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .clip(RoundedCornerShape(40.dp)),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        SelfBellBottomNavigation(navController = navController)
                    }
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