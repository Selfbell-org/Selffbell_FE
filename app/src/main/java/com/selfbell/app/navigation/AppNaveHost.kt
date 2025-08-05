package com.selfbell.app.navigation

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellBottomNavigation
import com.selfbell.app.ui.SplashScreen
import com.selfbell.feature.home.ui.HomeScreen
import com.example.auth.ui.LandingScreen
import com.example.auth.ui.LoginScreen
import com.example.auth.ui.SignUpScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    SelfBellTheme {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        // Routes where the bottom bar should be hidden
        val routesWithoutBottomBar = remember {
            setOf(
                AppRoute.SPLASH_ROUTE,
                AppRoute.LANDING_ROUTE,
                AppRoute.LOGIN_ROUTE,
                AppRoute.SIGNUP_ROUTE,
                AppRoute.HOME_ROUTE // Home 화면도 바텀바가 없는 경로로 추가
            )
        }
        val shouldShowBottomBar = currentRoute !in routesWithoutBottomBar

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                content = { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.SPLASH_ROUTE,
                        modifier = Modifier.padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = if (shouldShowBottomBar) {
                                paddingValues.calculateBottomPadding() + 96.dp
                            } else {
                                paddingValues.calculateBottomPadding()
                            }
                        )
                    ) {
                        composable(AppRoute.SPLASH_ROUTE) {
                            SplashScreen(navController = navController)
                        }
                        composable(AppRoute.HOME_ROUTE) { HomeScreen(navController = navController) }
                        composable(AppRoute.ALERTS_ROUTE) { Text(text = "알림 화면") }
                        composable(AppRoute.ESCORT_ROUTE) { Text(text = "동행 화면") }
                        composable(AppRoute.SETTINGS_ROUTE) { Text(text = "설정 화면") }
                        composable(AppRoute.FRIENDS_ROUTE) { Text(text = "친구 화면") }
                        composable(AppRoute.LANDING_ROUTE) { LandingScreen(
                            onLoginClick = { navController.navigate(AppRoute.LOGIN_ROUTE) },
                            onSignUpClick = { navController.navigate(AppRoute.SIGNUP_ROUTE) }
                        )}
                        composable(AppRoute.LOGIN_ROUTE) { Text("Login Screen") } // Placeholder for Login
                        composable(AppRoute.SIGNUP_ROUTE) {
                            var nickname by remember { mutableStateOf("") }
                            SignUpScreen(
                                nickname = nickname,
                                onNicknameChange = { nickname = it },
                                onRegister = { nickname = it },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }
                }
            )

            // The bottom bar is conditionally rendered here
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
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
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

@Composable
fun SignUpScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onRegister: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    Text("Sign Up Screen")
}

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Text("Landing Screen")
}
