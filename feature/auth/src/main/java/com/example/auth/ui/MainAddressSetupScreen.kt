package com.example.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.auth.ui.AuthUiState
import com.selfbell.auth.ui.AuthViewModel
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.moveOrAddMarker
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAddressSetupScreen(
    navController: NavController,
    // 📌 Receive address, lat, and lon as parameters
    address: String,
    lat: Double,
    lon: Double,
    viewModel: MainAddressSetupViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var marker by remember { mutableStateOf<Marker?>(null) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    var selectedAddrType by remember { mutableStateOf("집") }
    var isDirectInputSelected by remember { mutableStateOf(false) }
    var directInputName by remember { mutableStateOf("") }
    val directInputFocusRequester = remember { FocusRequester() }

    // 📌 API 호출 상태를 관찰합니다.
    val authUiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isDirectInputSelected) {
        if (isDirectInputSelected) {
            directInputFocusRequester.requestFocus()
        }
    }

    // uiState.userLatLng 값이 변경될 때만 마커를 업데이트하고 카메라 이동
    LaunchedEffect(uiState.userLatLng) {
        uiState.userLatLng?.let { pos ->
            naverMap?.let { map ->
                marker = moveOrAddMarker(map, pos, marker)
                map.moveCamera(CameraUpdate.scrollTo(pos))
            }
        }
    }

    // 📌 API 호출 성공/실패에 따른 로직 처리
    LaunchedEffect(authUiState) {
        when (authUiState) {
            is AuthUiState.Success -> {
                // API 호출 성공 시 다음 화면으로 이동
                navController.navigate(AppRoute.CONTACT_REGISTER_ROUTE)
            }
            is AuthUiState.Error -> {
                coroutineScope.launch {
                    val errorMessage = (authUiState as AuthUiState.Error).message
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // == 상단 온보딩 및 제목 영역 ==
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OnboardingProgressBar(
                        currentStep = 3,
                        totalSteps = 4
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "자주 이용하는 메인주소를\n등록해 주세요.",
                        style = Typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // == 지도 화면 영역 ==
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Box {
                        ReusableNaverMap(
                            modifier = Modifier.fillMaxSize(),
                            onMapReady = { map ->
                                naverMap = map
                                uiState.userLatLng?.let { pos ->
                                    marker = moveOrAddMarker(map, pos, marker)
                                    map.moveCamera(CameraUpdate.scrollTo(pos))
                                }
                            },
                            onLocationChanged = { }
                        )
                    }
                }

                // == 하단 설정 UI 영역 ==
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .weight(1f), // 하단 버튼을 제외한 남은 공간 차지
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AddressTypeButton(
                            text = "집",
                            isSelected = selectedAddrType == "집",
                            onClick = {
                                selectedAddrType = "집"
                                isDirectInputSelected = false
                                viewModel.updateAddrType("집")
                            },
                            icon = R.drawable.home_icon,
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeButton(
                            text = "학교",
                            isSelected = selectedAddrType == "학교",
                            onClick = {
                                selectedAddrType = "학교"
                                isDirectInputSelected = false
                                viewModel.updateAddrType("학교")
                            },
                            icon = R.drawable.school_icon,
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeButton(
                            text = "직접 입력",
                            isSelected = isDirectInputSelected,
                            onClick = {
                                isDirectInputSelected = true
                                selectedAddrType = "직접 입력"
                                directInputName = ""
                            },
                            icon = R.drawable.location_icon,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(
                        visible = isDirectInputSelected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedTextField(
                            value = directInputName,
                            onValueChange = { directInputName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(directInputFocusRequester),
                            placeholder = { Text("예: 회사, 학원") }
                        )
                    }
                }

                // 다음으로 버튼
                SelfBellButton(
                    text = if (authUiState is AuthUiState.Loading) "등록 중..." else "다음으로",
                    onClick = {
                        val name = if (isDirectInputSelected) directInputName else selectedAddrType
                        
                        // ✅ 실제 주소 등록 API 호출 (토큰은 AuthInterceptor에서 자동 추가)
                        authViewModel.registerMainAddress(
                            name = name,
                            address = address,
                            lat = lat,
                            lon = lon
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp)
                        .navigationBarsPadding(),
                    enabled = ((selectedAddrType == "직접 입력" && directInputName.isNotBlank()) || (selectedAddrType != "직접 입력")) && authUiState !is AuthUiState.Loading // 📌 로딩 중일 때 비활성화
                )
            }

            // 📌 로딩 중일 때 로딩 인디케이터 표시
            if (authUiState is AuthUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun AddressTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.Transparent,
            contentColor = if (isSelected) Color.Black else GrayInactive
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(1.dp, Color.Black) else BorderStroke(0.dp, GrayInactive),
        modifier = modifier
            .height(48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = if (isSelected) Color.Black else GrayInactive
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight(600)
                )
            )
        }
    }
}
@Preview
@Composable
fun MainAddressSetupScreenPreview() {
    SelfBellTheme {
        MainAddressSetupScreen(
            navController = rememberNavController(),
            // 📌 프리뷰를 위해 가상 데이터를 제공합니다.
            address = "서울시 동작구 상도로 369",
            lat = 37.4966895,
            lon = 126.9575041
        )
    }
}
