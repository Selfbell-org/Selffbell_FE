package com.example.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.example.auth.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PermissionScreen(
    //onLoginClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier) {
    // 온보딩의 총 단계 수 (프로필 -> 권한 -> 주소)
    val totalOnboardingSteps = 3
    val currentOnboardingStep = 2 // 현재 화면은 권한 설정 단계

    // 현재 완료된 권한의 개수를 저장하는 상태
    var currentPermissionCount by remember { mutableIntStateOf(0) }

    // 권한 요청 완료 시 다음 화면으로 자동 이동하는 로직

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 온보딩 프로그레스바 배치
            Spacer(modifier = Modifier.height(20.dp))
            OnboardingProgressBar(currentStep = 1, totalSteps = 4) // 총 5단계 중 1단계
            Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "원활한 앱 사용을 위해\n아래 권한을 확인해 주세요.",
                    style = Typography.bodyMedium,
                    color = Black
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 각 권한 항목을 PermissionItem으로 표시
                PermissionItem(
                    title = "위치 권한",
                    description = "현재 위치 주변의 성범죄자 정보를 제공합니다.",
                    leftIconResId = R.drawable.mappinline_icon,
                    state = if (currentPermissionCount >= 1) PermissionState.COMPLETED else PermissionState.ACTIVE,
                    onClick = { if (currentPermissionCount < 4) currentPermissionCount++ }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PermissionItem(
                    title = "백그라운드 위치 권한",
                    description = "앱을 사용하지 않을 때에도 사용자의 위치를 추적하여 알림을 보냅니다.",
                    leftIconResId = R.drawable.skipforward_icon,
                    state = if (currentPermissionCount >= 2) PermissionState.COMPLETED else PermissionState.INACTIVE,
                    onClick = { if (currentPermissionCount < 4) currentPermissionCount++ }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PermissionItem(
                    title = "푸시 알림 권한",
                    description = "실시간 알림 및 동선 공유 알림을 보냅니다.",
                    leftIconResId = R.drawable.bellringing_icon,
                    state = if (currentPermissionCount >= 3) PermissionState.COMPLETED else PermissionState.INACTIVE,
                    onClick = { if (currentPermissionCount < 4) currentPermissionCount++ }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PermissionItem(
                    title = "연락처 권한",
                    description = "지인에게 동선 공유 알림을 보내기 위해 연락처를 불러옵니다.",
                    leftIconResId = R.drawable.addressbook_icon,
                    state = if (currentPermissionCount >= 4) PermissionState.COMPLETED else PermissionState.INACTIVE,
                    onClick = { if (currentPermissionCount < 4) currentPermissionCount++ }
                )
            }

        SelfBellButton(
            text = "다음으로",
            onClick = {
                // 모든 권한 요청이 끝났으면 다음 단계인 주소 등록 페이지로 이동
                if (currentPermissionCount == 4) { // 모든 권한 요청이 끝난 후
                    navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE) // <-- 주소 등록 페이지로 이동
                }
            },
            modifier = Modifier.padding(bottom = 20.dp),
            // 모든 권한이 완료되었을 때만 버튼 활성화
            enabled = currentPermissionCount == 4
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    SelfBellTheme {
        PermissionScreen(navController = rememberNavController())
    }
}
