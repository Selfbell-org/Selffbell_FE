package com.example.auth.ui


import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.naver.maps.geometry.LatLng

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onAddressSet: (String, String, LatLng?) -> Unit,
    navController: NavController
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showMap by remember { mutableStateOf(false) }

    // 권한 요청 및 허용 처리
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }
    LaunchedEffect(locationPermissionState.status) {
        showMap = locationPermissionState.status.isGranted
    }

    if (showMap) {
        MainAddressSetupScreen (onAddressSet = onAddressSet, navController = navController)
    } else {
        // 권한 안내 UI
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("위치 권한이 필요합니다.")
        }
    }
}
