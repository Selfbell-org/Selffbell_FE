package com.selfbell.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.layout.widthIn

@Composable
fun AddressRegisterScreen(navController: NavController, modifier: Modifier = Modifier) {
    var searchAddress by remember { mutableStateOf("") }
    var isAddressSelected by remember { mutableStateOf(false) } // 주소 선택 상태
    val totalOnboardingSteps = 3
    val currentOnboardingStep = 3

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. 네이버 지도 API 영역 (가장 아래 레이어)
        ReusableNaverMap(modifier = Modifier.fillMaxSize())

        // 2. 지도 위에 겹쳐지는 UI들을 Column으로 배치
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                OnboardingProgressBar(currentStep = currentOnboardingStep, totalSteps = totalOnboardingSteps)
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "자주 이용하는 메인주소를\n등록해 주세요.",
                    style = Typography.headlineMedium,
                    color = Black
                )
                Spacer(modifier = Modifier.height(32.dp))

                // 주소 검색 전 상태 UI
                if (!isAddressSelected) {
                    TextField(
                        value = searchAddress,
                        onValueChange = { searchAddress = it },
                        label = { Text("주소 검색") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색 아이콘") },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SelfBellButton(
                        text = "현재 위치로 찾기",
                        onClick = {
                            // TODO: 현재 위치로 지도 이동 로직 구현
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 검색 결과 또는 주소 선택 후 UI
            if (isAddressSelected) {
                // 주소 선택 후 UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(text = "선택된 주소", style = Typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = searchAddress, style = Typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelfBellButton(
                            text = "집",
                            onClick = { /* TODO */ },
                            buttonType = SelfBellButtonType.OUTLINED,
                            isSmall = true,
                            modifier = Modifier.weight(1f)
                        )
                        SelfBellButton(
                            text = "학교",
                            onClick = { /* TODO */ },
                            buttonType = SelfBellButtonType.OUTLINED,
                            isSmall = true,
                            modifier = Modifier.weight(1f)
                        )
                        SelfBellButton(
                            text = "직접입력",
                            onClick = { /* TODO */ },
                            buttonType = SelfBellButtonType.OUTLINED,
                            isSmall = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // 검색 예시 안내
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(text = "이렇게 검색해 보세요", style = Typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "・도로명 + 건물번호 (위례성대로 2)", style = Typography.bodyMedium.copy(color = Color.Gray))
                    Text(text = "・건물명 + 번지 (방이동 44-2)", style = Typography.bodyMedium.copy(color = Color.Gray))
                    Text(text = "・건물명 + 아파트명 (반포 자이, 분당 주공 1차)", style = Typography.bodyMedium.copy(color = Color.Gray))
                }
            }

            // "다음으로" 버튼
            SelfBellButton(
                text = "다음으로",
                onClick = {
                    // 주소 등록 완료 후 홈 화면으로 이동
                    navController.navigate(AppRoute.HOME_ROUTE)
                },
                modifier = Modifier.padding(bottom = 20.dp),
                enabled = isAddressSelected // 주소 선택 시에만 활성화
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddressRegisterScreenPreview() {
    SelfBellTheme {
        AddressRegisterScreen(navController = rememberNavController())
    }
}
