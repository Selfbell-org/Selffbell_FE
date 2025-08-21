package com.selfbell.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.ui.AddressRegisterScreen
import com.example.auth.ui.ContactRegistrationScreen
import com.example.auth.ui.LandingScreen
import com.example.auth.ui.LoginScreen
import com.example.auth.ui.MainAddressSetupScreen
import com.example.auth.ui.OnboardingCompleteScreen
import com.example.auth.ui.PasswordScreen
import com.example.auth.ui.PhoneNumberScreen
import com.example.auth.ui.PermissionScreen
import com.example.auth.ui.PhoneNumberLoginScreen
import com.example.auth.ui.ProfileRegisterScreen
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.alerts.ui.AlertsScreen
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellBottomNavigation
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.escort.ui.EscortScreen
import com.selfbell.home.ui.HomeScreen
import com.selfbell.settings.ui.SettingsScreen
import com.selfbell.app.ui.SplashScreen
import com.selfbell.escort.ui.AddressSearchScreen
import com.selfbell.settings.ui.ContactListScreen // ✅ New screen import


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val routesWithoutBottomBar = remember {
        setOf(
            AppRoute.SPLASH_ROUTE,
            AppRoute.LANDING_ROUTE,
            AppRoute.PHONE_NUMBER_LOGIN_ROUTE,
            AppRoute.LOGIN_PIN_ROUTE_WITH_ARGS,
            AppRoute.PROFILE_REGISTER_ROUTE_WITH_ARGS,
            AppRoute.PERMISSION_ROUTE,
            AppRoute.ADDRESS_REGISTER_ROUTE,
            AppRoute.CONTACT_REGISTER_ROUTE,
            AppRoute.ONBOARDING_COMPLETE_ROUTE,
            AppRoute.PHONE_NUMBER_ROUTE,
            AppRoute.PASSWORD_ROUTE_WITH_ARGS,
            AppRoute.MAIN_ADDRESS_SETUP_ROUTE_WITH_ARGS
        )
    }
    val shouldShowBottomBar = currentRoute !in routesWithoutBottomBar
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var currentMapMarker by remember { mutableStateOf<Marker?>(null) }

    SelfBellTheme {
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
                            0.dp
                        }
                    )
                ) {
                    composable(AppRoute.SPLASH_ROUTE) { SplashScreen(navController = navController) }
                    composable(AppRoute.HOME_ROUTE) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onMsgReportClick = { println("Msg report clicked in Navhost") }
                        )
                    }

                    composable(AppRoute.ALERTS_ROUTE) { AlertsScreen() }
                    composable(AppRoute.ESCORT_ROUTE) { EscortScreen(navController) }

                    composable(AppRoute.ADDRESS_SEARCH_ROUTE) {
                        AddressSearchScreen(
                            navController = navController,
                            onAddressSelected = { address, lat, lon ->
                                // ✅ 결과를 이전 화면(EscortScreen)의 ViewModel로 전달하고 뒤로가기
                                navController.previousBackStackEntry
                                    ?.savedStateHandle?.set("address_name", address)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle?.set("address_lat", lat)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle?.set("address_lon", lon)
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(AppRoute.SETTINGS_ROUTE) { SettingsScreen(navController = navController) }
                    composable(AppRoute.FRIENDS_ROUTE) { }

                    composable(AppRoute.LANDING_ROUTE) {
                        LandingScreen(
                            onLoginClick = { navController.navigate(AppRoute.PHONE_NUMBER_LOGIN_ROUTE) },
                            onSignUpClick = { navController.navigate(AppRoute.PHONE_NUMBER_ROUTE) }
                        )
                    }

                    // 📌 회원가입 플로우
                    composable(AppRoute.PHONE_NUMBER_ROUTE) {
                        PhoneNumberScreen(
                            onConfirmClick = { phoneNumber ->
                                navController.navigate(AppRoute.passwordRoute(phoneNumber))
                            }
                        )
                    }

                    composable(AppRoute.FRIENDS_ROUTE) {
                        ContactListScreen(navController = navController)
                    }
                    // 📌 로그인 플로우
                    composable(AppRoute.PHONE_NUMBER_LOGIN_ROUTE) {
                        PhoneNumberLoginScreen(
                            onConfirmClick = { phoneNumber ->
                                navController.navigate(AppRoute.loginPinRoute(phoneNumber))
                            }
                        )
                    }

                    composable(
                        route = AppRoute.LOGIN_PIN_ROUTE_WITH_ARGS,
                        arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                        LoginScreen(
                            phoneNumber = phoneNumber,
                            onLoginSuccess = {
                                navController.navigate(AppRoute.ONBOARDING_COMPLETE_ROUTE) {
                                    popUpTo(AppRoute.LANDING_ROUTE) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = AppRoute.PASSWORD_ROUTE_WITH_ARGS,
                        arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                        PasswordScreen(
                            phoneNumber = phoneNumber,
                            onConfirmClick = { password ->
                                navController.navigate(AppRoute.profileRegisterRoute(phoneNumber, password))
                            }
                        )
                    }

                    composable(
                        route = AppRoute.PROFILE_REGISTER_ROUTE_WITH_ARGS,
                        arguments = listOf(
                            navArgument("phoneNumber") { type = NavType.StringType },
                            navArgument("password") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                        val password = backStackEntry.arguments?.getString("password") ?: ""
                        ProfileRegisterScreen(
                            navController = navController,
                            phoneNumber = phoneNumber,
                            password = password
                        )
                    }

                    composable(AppRoute.CONTACT_REGISTER_ROUTE) {
                        ContactRegistrationScreen(navController = navController)
                    }

                    composable(AppRoute.ONBOARDING_COMPLETE_ROUTE) {
                        OnboardingCompleteScreen(navController = navController)
                    }

                    // 📌 AddressRegisterScreen에 onNextClick 콜백 추가
                    composable(AppRoute.ADDRESS_REGISTER_ROUTE) {
                        AddressRegisterScreen(
                            navController = navController
                        )
                    }

                    composable(AppRoute.PERMISSION_ROUTE) {
                        PermissionScreen(navController = navController)
                    }

                    composable(AppRoute.REUSABEL_MAP) {
                        ReusableNaverMap(
                            modifier = Modifier.fillMaxSize(),
                            onMapReady = { map ->
                                naverMapInstance = map
                                println("NaverMap 준비 완료 in AppNavHost!")
                                map.uiSettings.isCompassEnabled = true
                                map.uiSettings.isZoomControlEnabled = true
                                map.uiSettings.isLocationButtonEnabled = true
                                val initialPosition = LatLng(37.5665, 126.9780)
                                map.moveCamera(CameraUpdate.scrollTo(initialPosition))
                                val marker = Marker()
                                marker.position = initialPosition
                                marker.captionText = "초기 위치"
                                marker.map = map
                                currentMapMarker = marker
                            }
                        )
                    }

                    composable(
                        route = AppRoute.MAIN_ADDRESS_SETUP_ROUTE_WITH_ARGS,
                        arguments = listOf(
                            navArgument("address") { type = NavType.StringType },
                            navArgument("lat") { type = NavType.FloatType },
                            navArgument("lng") { type = NavType.FloatType }
                        )
                    ) { backStackEntry ->
                        val address = backStackEntry.arguments?.getString("address") ?: ""
                        val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
                        val lng = backStackEntry.arguments?.getFloat("lng")?.toDouble() ?: 0.0

                        MainAddressSetupScreen(
                            navController = navController,
                            address = address,
                            lat = lat,
                            lon = lng
                        )
                    }
                }
            }
        )

        if (shouldShowBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomCenter
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

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost(navController = rememberNavController())
}