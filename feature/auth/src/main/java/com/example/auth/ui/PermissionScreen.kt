package com.example.auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.example.auth.R
import android.Manifest
import androidx.compose.ui.tooling.preview.Preview
import com.selfbell.core.ui.theme.Primary

@Composable
fun PermissionScreen(navController: NavController) {
    val totalOnboardingSteps = 3
    val currentOnboardingStep = 2 // 권한 설정은 온보딩의 2단계에 해당합니다.

    // 현재 완료된 권한의 개수를 저장하는 상태
    var currentPermissionCount by remember { mutableIntStateOf(0) }

    // 각 권한 요청 결과를 저장하는 상태들
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var backgroundLocationPermissionGranted by remember { mutableStateOf(false) }
    var pushNotificationPermissionGranted by remember { mutableStateOf(false) }
    var contactsPermissionGranted by remember { mutableStateOf(false) }

    // 권한 요청 완료 시 다음 화면으로 자동 이동하는 로직
    LaunchedEffect(currentPermissionCount) {
        if (currentPermissionCount == 4) {
            navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE)
        }
    }

    // 권한 요청 런처들
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (isGranted) currentPermissionCount++
    }
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        backgroundLocationPermissionGranted = isGranted
        if (isGranted) currentPermissionCount++
    }
    val pushNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        pushNotificationPermissionGranted = isGranted
        if (isGranted) currentPermissionCount++
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        contactsPermissionGranted = isGranted
        if (isGranted) currentPermissionCount++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 70.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        OnboardingProgressBar(currentStep = currentOnboardingStep, totalSteps = totalOnboardingSteps)
        Spacer(modifier = Modifier.height(40.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "권한을 확인해주세요",
                style = Typography.headlineMedium,
                color = Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "원활한 앱 사용을 위해\n아래 권한을 확인해 주세요.",
                style = Typography.bodyMedium,
                color = Black
            )
            Spacer(modifier = Modifier.height(40.dp))

            PermissionItem(
                title = "위치 권한",
                description = "현재 위치 주변의 성범죄자 정보를 제공합니다.",
                leftIconResId = R.drawable.mappinline_icon,
                state = if (locationPermissionGranted) PermissionState.COMPLETED else PermissionState.ACTIVE,
                onClick = {
                    if (!locationPermissionGranted) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "백그라운드 위치 권한",
                description = "앱을 사용하지 않을 때에도 사용자의 위치를 추적하여 알림을 보냅니다.",
                leftIconResId = R.drawable.skipforward_icon,
                state = if (backgroundLocationPermissionGranted) PermissionState.COMPLETED else PermissionState.INACTIVE,
                onClick = {
                    if (locationPermissionGranted && !backgroundLocationPermissionGranted) {
                        backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "푸시 알림 권한",
                description = "실시간 알림 및 동선 공유 알림을 보냅니다.",
                leftIconResId = R.drawable.bellringing_icon,
                state = if (pushNotificationPermissionGranted) PermissionState.COMPLETED else PermissionState.INACTIVE,
                onClick = {
                    if (backgroundLocationPermissionGranted && !pushNotificationPermissionGranted) {
                        pushNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "연락처 권한",
                description = "지인에게 동선 공유 알림을 보내기 위해 연락처를 불러옵니다.",
                leftIconResId = R.drawable.addressbook_icon,
                state = if (contactsPermissionGranted) PermissionState.COMPLETED else PermissionState.INACTIVE,
                onClick = {
                    if (pushNotificationPermissionGranted && !contactsPermissionGranted) {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SelfBellButton(
            text = "다음으로",
            onClick = {
                if (currentPermissionCount == 4) {
                    navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE)
                }
            },
            modifier = Modifier.padding(bottom = 20.dp),
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
