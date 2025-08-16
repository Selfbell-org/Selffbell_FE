package com.example.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.moveOrAddMarker
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Pretendard // Pretendard 폰트 import

@Composable
fun MainAddressSetupScreen(
    navController: NavController,
    viewModel: MainAddressSetupViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var marker by remember { mutableStateOf<Marker?>(null) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    var selectedAddrType by remember { mutableStateOf("집") }
    var isDirectInputSelected by remember { mutableStateOf(false) }
    var directInputName by remember { mutableStateOf("") }
    val directInputFocusRequester = remember { FocusRequester() }

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

    Box(
        modifier = modifier.fillMaxSize()
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
                shadowElevation = 8.dp // 그림자 효과 추가
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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 장소 선택 버튼 Row
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

                // 직접 입력 필드
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

            Spacer(Modifier.height(16.dp))

            // 다음으로 버튼
            SelfBellButton(
                text = "다음으로",
                onClick = {
                    // 선택된 별칭을 ViewModel로 전달
                    if (isDirectInputSelected) {
                        viewModel.updateAddrType(directInputName)
                    } else {
                        viewModel.updateAddrType(selectedAddrType)
                    }
                    viewModel.setMainAddress()
                    navController.navigate(AppRoute.CONTACT_REGISTER_ROUTE)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .navigationBarsPadding(),
                enabled = uiState.address.isNotBlank() || uiState.userLatLng != null
            )
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
            .height(48.dp) // height 고정
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
fun MainAddressSetupScreenPreview(){
    MainAddressSetupScreen(navController = rememberNavController())
}