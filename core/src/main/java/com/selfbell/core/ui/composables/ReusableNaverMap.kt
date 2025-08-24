package com.selfbell.core.ui.composables // 실제 경로에 맞게 수정 필요 (예: com.selfbell.core.ui.maps)

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource // Naver의 FusedLocationSource
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding

@SuppressLint("MissingPermission")
@Composable
fun ReusableNaverMap(
    modifier: Modifier = Modifier,
    cameraPosition: LatLng? = LatLng(37.5665, 126.9780),
    onMapReady: (NaverMap) -> Unit,
    onLocationChanged: ((LatLng) -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val locationSource = remember(context) {
        if (context is Activity) {
            FusedLocationSource(context, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            null
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier) { view ->
        view.getMapAsync { naverMap ->
            // 초기 카메라 위치 설정
            cameraPosition?.let {
                naverMap.moveCamera(CameraUpdate.scrollTo(it))
            }

            if (locationSource != null &&
                (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            ) {
                naverMap.locationSource = locationSource
                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow // 기본은 NoFollow, 필요시 Follow/Face로 변경
            } else {
                // 권한이 없거나 locationSource가 null이면 위치 관련 UI 비활성화
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            naverMap.uiSettings.isLocationButtonEnabled = locationSource != null // 위치 버튼은 소스가 있을 때만 활성화

            onLocationChanged?.let { callback ->
                naverMap.addOnLocationChangeListener { location: Location ->
                    callback(LatLng(location.latitude, location.longitude))
                }
            }

            onMapClicked?.let { callback ->
                naverMap.setOnMapClickListener { _, latLng ->
                    callback(latLng)
                }
            }
            onMapReady(naverMap)
        }
    }
}

// FusedLocationSource에서 사용할 요청 코드 (실제 권한 요청은 외부에서 관리)
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

// 지도에 마커 하나만 추가/이동 (이 함수는 MainAddressSetupScreen으로 옮기거나, 유틸리티로 분리 가능)
fun moveOrAddMarker(
    naverMap: NaverMap?,
    position: LatLng,
    currentMarker: Marker?
): Marker {
    val marker = currentMarker ?: Marker()
    marker.position = position
    if (marker.map == null) { // 마커가 지도에 추가되지 않은 경우에만 map 설정
        marker.map = naverMap
    }
    // 필요시 마커 아이콘 등 설정
    // marker.iconTintColor = Color.RED // 예시
    return marker
}
