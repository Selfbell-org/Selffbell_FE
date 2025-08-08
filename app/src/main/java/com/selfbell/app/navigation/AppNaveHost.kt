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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.data.position
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellBottomNavigation
import com.selfbell.app.ui.SplashScreen
import com.example.auth.ui.LandingScreen
import com.example.auth.ui.LoginScreen
import com.example.auth.ui.PermissionScreen
import com.example.auth.ui.SignUpScreen
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.home.ui.HomeScreen
import com.selfbell.home.ui.HomeViewModel

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
                AppRoute.PERMISSTION_ROUTE
            )
        }
        val shouldShowBottomBar = currentRoute !in routesWithoutBottomBar
        var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
        // 지도 화면에서 사용할 마커 참조 (선택적)
        var currentMapMarker by remember { mutableStateOf<Marker?>(null) }


        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                content = { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.SPLASH_ROUTE,
                        modifier = Modifier.padding(
                            top = paddingValues.calculateTopPadding(),
                            // shouldShowBottomBar가 true일 때만 바텀바 높이만큼 패딩을 줍니다.
                            // false일 때는 0dp를 주어 콘텐츠가 화면 끝까지 확장되도록 합니다.
                            bottom = if (shouldShowBottomBar) {
                                paddingValues.calculateBottomPadding() + 96.dp // 바텀바 높이 + 하단 패딩
                            } else {
                                0.dp // 바텀바가 없을 때는 하단 패딩을 주지 않아 콘텐츠가 바닥까지 확장
                            }
                        )
                    ) {
                        composable(AppRoute.SPLASH_ROUTE) { SplashScreen(navController = navController) }



                        composable(AppRoute.HOME_ROUTE) {
                            val homeViewModel: HomeViewModel = hiltViewModel()

                            // ViewModel의 StateFlow들을 Composable이 관찰할 수 있는 State로 변환
                            val userLatLng by homeViewModel.userLatLng.collectAsState()
                            val userAddress by homeViewModel.userAddress.collectAsState()
                            val userProfileImg by homeViewModel.userProfileImg.collectAsState()
                            val userProfileName by homeViewModel.userProfileName.collectAsState()
                            val criminalMarkers by homeViewModel.criminalMarkers.collectAsState()
                            val safetyBellMarkers by homeViewModel.safetyBellMarkers.collectAsState()
                            val searchedLatLng by homeViewModel.searchedLatLng.collectAsState()

                            // AddressSearchModal에서 사용될 상태 및 콜백 (ViewModel에서 가져온다고 가정)
                            val searchText by homeViewModel.searchText.collectAsState() // ViewModel에 searchText: StateFlow<String> 필요
                            // val searchResults by homeViewModel.searchResults.collectAsState() // 필요하다면 검색 결과도 ViewModel에서 관리

                            HomeScreen(
                                userLatLng = userLatLng,
                                userAddress = userAddress,
                                userProfileImg = userProfileImg,
                                userProfileName = userProfileName,
                                criminalMarkers = criminalMarkers,
                                safetyBellMarkers = safetyBellMarkers,
                                searchText = searchText, // ViewModel의 searchText 전달
                                onSearchTextChange = { newText -> // ViewModel의 함수 호출
                                    homeViewModel.onSearchTextChanged(newText) // ViewModel에 onSearchTextChanged(String) 함수 필요
                                },
                                onSearchClick = { // ViewModel의 함수 호출
                                    homeViewModel.onSearchConfirmed()       // ViewModel에 onSearchConfirmed() 함수 필요
                                },
                                onModalMarkerItemClick = { mapMarkerData -> // ViewModel의 함수 호출 또는 지도 직접 제어
                                    // 예시: 클릭된 마커의 위치로 지도 이동
                                    homeViewModel.onMapMarkerClicked(mapMarkerData) // ViewModel에 onMapMarkerClicked(MapMarkerData) 함수 필요
                                    // 또는 navController.navigate(...) 등으로 상세 화면 이동
                                    println("Marker clicked in NavHost: ${mapMarkerData.address}")
                                },
                                searchedLatLng = searchedLatLng,
                                onMsgReportClick = {
                                    // TODO: 메시지 신고 기능 구현 (ViewModel 함수 호출 등)
                                    homeViewModel.onReportMessageClicked() // ViewModel에 onReportMessageClicked() 함수 필요 (예시)
                                    println("Message report clicked in NavHost")
                                }
                            )
                        }
                        composable(AppRoute.ALERTS_ROUTE) { Text(text = "알림 화면") }
                        composable(AppRoute.ESCORT_ROUTE) { Text(text = "동행 화면") }
                        composable(AppRoute.SETTINGS_ROUTE) { Text(text = "설정 화면") }
                        composable(AppRoute.FRIENDS_ROUTE) { Text(text = "친구 화면") }
                        composable(AppRoute.LANDING_ROUTE) { LandingScreen(
                            onLoginClick = { navController.navigate(AppRoute.LOGIN_ROUTE) },
                            onSignUpClick = { navController.navigate(AppRoute.SIGNUP_ROUTE) }
                        )}
                        composable(AppRoute.LOGIN_ROUTE) { LoginScreen( onNavigateUp ={navController.popBackStack()}) } // Placeholder for Login
                        composable(AppRoute.SIGNUP_ROUTE) {
                            var nickname by remember { mutableStateOf("") }
                            SignUpScreen(
                                nickname = nickname,
                                onNicknameChange = { nickname = it },
                                onRegister = { navController.navigate((AppRoute.PERMISSTION_ROUTE))},
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable(AppRoute.REUSABEL_MAP) { ReusableNaverMap(
                            modifier = Modifier.fillMaxSize(),
                            onMapReady = { map ->
                                naverMapInstance = map // NaverMap 객체 저장
                                println("NaverMap 준비 완료 in AppNavHost!")

                                // 예시: 지도 준비 시 초기 설정
                                map.uiSettings.isCompassEnabled = true
                                map.uiSettings.isZoomControlEnabled = true
                                map.uiSettings.isLocationButtonEnabled = true // 위치 권한 및 LocationSource 설정 필요

                                // 예시: 특정 위치로 카메라 이동
                                val initialPosition = LatLng(37.5665, 126.9780)
                                map.moveCamera(CameraUpdate.scrollTo(initialPosition))

                                // 예시: 초기 마커 추가
                                val marker = Marker()
                                marker.position = initialPosition
                                marker.captionText = "초기 위치"
                                marker.map = map
                                currentMapMarker = marker

                                // 필요하다면 추가적인 지도 설정 수행
                            }
                        ) }
                        composable(AppRoute.PERMISSTION_ROUTE){ PermissionScreen(navController = navController, onAddressSet = { address, type, latLng ->
                            // 이 람다 함수가 onAddressSet 콜백입니다.
                            // PermissionScreen -> MainAddressSetupScreen 에서 주소 설정이 완료되면 호출됩니다.

                            println("주소 설정 완료 in AppNavHost:")
                            println("주소: $address")
                            println("유형: $type")
                            println("좌표: $latLng")

                            // TODO: 여기서 ViewModel에 주소 정보를 저장하거나 다른 작업 수행

                            // 예시: 주소 설정 후 홈 화면으로 이동하고 이전 스택을 정리
                            navController.navigate(AppRoute.HOME_ROUTE) {
                                popUpTo(AppRoute.PERMISSTION_ROUTE) { inclusive = true } // 권한 화면은 스택에서 제거
                                // 필요하다면 Landing/SignUp 등도 스택에서 제거
                                // popUpTo(AppRoute.LANDING_ROUTE) { inclusive = true }
                            }  } )}
                    }
                }
            )

            // The bottom bar is conditionally rendered here
            if (shouldShowBottomBar) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp) // 바텀바 자체의 하단 여백
                        .padding(horizontal = 24.dp) // 바텀바 자체의 좌우 여백
                        .navigationBarsPadding(), // 시스템 내비게이션 바 영역에 대한 패딩
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
