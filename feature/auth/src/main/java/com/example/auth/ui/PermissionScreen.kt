package com.example.auth.ui

import PermissionItem
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography

// PermissionItem과 PermissionState는 프로젝트에 맞게 정의되어 있어야 합니다.
//enum class PermissionState { ACTIVE, INACTIVE, COMPLETED }

@Composable
fun PermissionScreen(navController: NavController) {
    val totalOnboardingSteps = 3
    val currentOnboardingStep = 2

    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // 각 권한 요청 결과를 저장하는 상태들
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var backgroundLocationPermissionGranted by remember { mutableStateOf(false) }
    var pushNotificationPermissionGranted by remember { mutableStateOf(false) }
    var contactsPermissionGranted by remember { mutableStateOf(false) }

    // 권한 상태를 업데이트하는 헬퍼 함수
    val updatePermissionStates: () -> Unit = {
        // 백그라운드 위치 권한이 허용되면, 전경 위치 권한도 자동으로 허용된 것으로 간주합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            locationPermissionGranted = backgroundLocationPermissionGranted || (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED)
        } else {
            locationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            backgroundLocationPermissionGranted = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushNotificationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            pushNotificationPermissionGranted = true
        }
        contactsPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // `DisposableEffect`를 사용하여 화면이 활성화될 때마다 권한 상태를 재확인합니다.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updatePermissionStates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 권한 요청 런처들
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
    }

    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        backgroundLocationPermissionGranted = isGranted
    }
    val pushNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        pushNotificationPermissionGranted = isGranted
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        contactsPermissionGranted = isGranted
    }

    // 모든 권한이 허용되면 다음 화면으로 이동
    LaunchedEffect(locationPermissionGranted, backgroundLocationPermissionGranted, pushNotificationPermissionGranted, contactsPermissionGranted) {
        if (locationPermissionGranted && backgroundLocationPermissionGranted && pushNotificationPermissionGranted && contactsPermissionGranted) {
            navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE)
        }
    }

    // `shouldShowRequestPermissionRationale()` 함수로 권한 요청 로직을 분기하는 헬퍼 함수
    fun requestPermissionOrOpenSettings(permission: String, launcher: ActivityResultLauncher<String>, granted: Boolean) {
        if (!granted) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                launcher.launch(permission)
            } else {
                Toast.makeText(context, "권한을 허용하려면 설정으로 이동해주세요.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
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
                    requestPermissionOrOpenSettings(Manifest.permission.ACCESS_FINE_LOCATION, locationPermissionLauncher, locationPermissionGranted)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "백그라운드 위치 권한",
                description = "앱을 사용하지 않을 때에도 사용자의 위치를 추적하여 알림을 보냅니다.",
                leftIconResId = R.drawable.skipforward_icon,
                state = if (backgroundLocationPermissionGranted) PermissionState.COMPLETED else if (locationPermissionGranted) PermissionState.ACTIVE else PermissionState.INACTIVE,
                onClick = {
                    if (locationPermissionGranted && !backgroundLocationPermissionGranted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Toast.makeText(context, "위치 권한을 '항상 허용'으로 변경해야 합니다.", Toast.LENGTH_LONG).show()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    } else if (!locationPermissionGranted) {
                        Toast.makeText(context, "위치 권한을 먼저 허용해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "푸시 알림 권한",
                description = "실시간 알림 및 동선 공유 알림을 보냅니다.",
                leftIconResId = R.drawable.bellringing_icon,
                state = if (pushNotificationPermissionGranted) PermissionState.COMPLETED else if (backgroundLocationPermissionGranted) PermissionState.ACTIVE else PermissionState.INACTIVE,
                onClick = {
                    if (backgroundLocationPermissionGranted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionOrOpenSettings(Manifest.permission.POST_NOTIFICATIONS, pushNotificationPermissionLauncher, pushNotificationPermissionGranted)
                        } else {
                            pushNotificationPermissionGranted = true
                        }
                    } else {
                        Toast.makeText(context, "백그라운드 위치 권한을 먼저 허용해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionItem(
                title = "연락처 권한",
                description = "지인에게 동선 공유 알림을 보내기 위해 연락처를 불러옵니다.",
                leftIconResId = R.drawable.addressbook_icon,
                state = if (contactsPermissionGranted) PermissionState.COMPLETED else if (pushNotificationPermissionGranted) PermissionState.ACTIVE else PermissionState.INACTIVE,
                onClick = {
                    if (pushNotificationPermissionGranted) {
                        requestPermissionOrOpenSettings(Manifest.permission.READ_CONTACTS, contactsPermissionLauncher, contactsPermissionGranted)
                    } else {
                        Toast.makeText(context, "푸시 알림 권한을 먼저 허용해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SelfBellButton(
            text = "다음으로",
            onClick = {
                if (locationPermissionGranted && backgroundLocationPermissionGranted && pushNotificationPermissionGranted && contactsPermissionGranted) {
                    navController.navigate(AppRoute.ADDRESS_REGISTER_ROUTE)
                }
            },
            modifier = Modifier.padding(bottom = 20.dp),
            enabled = locationPermissionGranted && backgroundLocationPermissionGranted && pushNotificationPermissionGranted && contactsPermissionGranted
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