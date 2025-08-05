package com.selfbell.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.map.MapView

@Composable
fun ReusableNaverMap(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) } // 일단 주석
    // val activity = context.findActivity() // 일단 주석
    // val locationSource: FusedLocationSource? = remember(activity) { // 일단 주석
    //     activity?.let { FusedLocationSource(it, BASIC_LOCATION_PERMISSION_REQUEST_CODE) }
    // }

    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy() // 여기서만 호출하거나 onDispose에서만 호출
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // mapView.onDestroy() // 또는 여기서 호출하고 Lifecycle.Event.ON_DESTROY 에서는 호출 안 함
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { mapView }
        ) {
            // 이 블록은 update 시 호출됨, 지금은 비워둠
        }

        // NaverMap 객체 비동기 로드 및 현재 위치 설정 -> 전체 주석 처리
        /*
        LaunchedEffect(mapView, locationSource) {
            mapView.getMapAsync { map ->
                // naverMapInstance = map // 일단 주석
                // map.uiSettings.isLocationButtonEnabled = true // 일단 주석

                // ... (이하 모든 위치 및 권한 관련 로직 주석 처리)
            }
        }
        */

        // 상태 메시지 표시 로직 -> 전체 주석 처리
        /*
        if (naverMapInstance == null) {
            Text("지도 로딩 중...", modifier = Modifier.align(Alignment.Center))
        } // ... (이하 모든 조건부 Text 주석 처리)
        */
        Text("지도 표시 테스트", modifier = Modifier.align(Alignment.Center)) // 간단한 확인용 텍스트
    }
}
    