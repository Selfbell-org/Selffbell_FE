package com.selfbell.home.ui // 실제 HomeScreen.kt의 패키지 경로

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReusableNaverMap // ReusableNaverMap 경로
import com.selfbell.home.model.MapMarkerData // MapMarkerData 경로
import com.selfbell.core.ui.theme.Typography // Typography 경로 (AddressSearchModal에서 사용)
import com.selfbell.feature.home.R // R 파일 경로 (ic_search, sos_icon, criminal_icon 등)
import kotlinx.coroutines.launch
@Composable
fun HomeScreen(
    userLatLng: LatLng,
    userAddress: String,
    userProfileImg: Int,
    userProfileName: String,
    criminalMarkers: List<MapMarkerData>,     // 범죄자 마커 리스트
    safetyBellMarkers: List<MapMarkerData>,   // 안심벨 마커 리스트

    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onModalMarkerItemClick: (MapMarkerData) -> Unit, // AddressSearchModal의 아이템 클릭 콜백
    searchedLatLng: LatLng?,
    onMsgReportClick: () -> Unit

) {
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var cameraPosition by remember {
        mutableStateOf(
            searchedLatLng ?: if (userLatLng.latitude != 0.0 || userLatLng.longitude != 0.0) {
                userLatLng
            } else {
                LatLng(37.5665, 126.9780)
            }
        )
    }
    var infoWindowData by remember { mutableStateOf<Pair<LatLng, String>?>(null) }

    // AddressSearchModal에 표시할 마커 리스트 (ViewModel에서 미리 준비하는 것이 더 좋음)
    val modalMapMarkers = remember(criminalMarkers, safetyBellMarkers) {
        // 필요에 따라 필터링, 정렬, 거리 계산 등을 여기서 또는 ViewModel에서 수행
        // 예시: 단순히 합치기. 실제로는 거리순 정렬 등이 필요할 수 있음
        (criminalMarkers + safetyBellMarkers).sortedBy {
            // 사용자 위치와 마커 위치 간의 거리를 계산하여 정렬할 수 있음
            // 여기서는 임시로 타입으로 정렬하거나, MapMarkerData에 distance 필드가 있다면 그걸로 정렬
            it.distance // 또는 it.latLng.distanceTo(userLatLng) 등을 사용 (거리 계산 로직 필요)
        }
    }

    Box(Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = cameraPosition,
            onMapReady = { map ->
                naverMapInstance = map
                if (searchedLatLng != null) map.moveCamera(CameraUpdate.scrollTo(searchedLatLng))
                else if (userLatLng.latitude != 0.0 || userLatLng.longitude != 0.0) map.moveCamera(CameraUpdate.scrollTo(userLatLng))

                addOrUpdateMarker(map, userLatLng, MapMarkerData(userLatLng, userAddress,
                    MapMarkerData.MarkerType.USER, "현재 위치")) { infoWindowData = it }
                criminalMarkers.forEach { data -> addOrUpdateMarker(map, data.latLng, data) { infoWindowData = it } }
                safetyBellMarkers.forEach { data -> addOrUpdateMarker(map, data.latLng, data) { infoWindowData = it } }
            }
        )

        infoWindowData?.let { (latLngValue, addressValue) ->
            MapInfoBalloon(address = addressValue, latLng = latLngValue, onDismissRequest = { infoWindowData = null })
        }

        // ===== 상단 박스 (프로필 UI) =====
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp)
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White.copy(alpha = 0.95f)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(userProfileImg),
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(999.dp)) // 원형 이미지
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("profile", style = Typography.labelSmall, color = Color(0xFF949494))
                    Text(userProfileName, style = Typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onMsgReportClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.msg_report_icon),
                        contentDescription = "메시지 신고",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // ===== 하단 모달 (AddressSearchModal) =====
        AddressSearchModal(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onSearchClick = onSearchClick,
            mapMarkers = modalMapMarkers, // 수정된 마커 리스트 전달
            onMarkerItemClick = onModalMarkerItemClick // 수정된 콜백 전달
        )
    }
}

fun addOrUpdateMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (Pair<LatLng, String>) -> Unit
) {
    Marker().apply {
        position = latLng
        map = naverMap
        iconTintColor = when (data.type) {
            MapMarkerData.MarkerType.USER -> Color(0xFF2962FF).hashCode() // 파랑
            MapMarkerData.MarkerType.CRIMINAL -> Color(0xFFD32F2F).hashCode() // 빨강
            MapMarkerData.MarkerType.SAFETY_BELL -> Color(0xFF43A047).hashCode() // 초록
        }
        setOnClickListener {
            onClick(latLng to data.address)
            true
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HomeScreenPreview() {
    val sampleUserLatLng = LatLng(37.5665, 126.9780)
    val sampleUserAddress = "서울 중구 세종대로 110"
    val sampleUserProfileImg = R.drawable.usre_profileimg_icon // 실제 리소스 ID로 교체
    val sampleUserProfileName = "홍길동"

    // MapMarkerData에 distance 필드가 있다면 채워주거나, 동적으로 계산하는 로직이 ViewModel에 있다고 가정
    val sampleCriminalMarkers = listOf(
        MapMarkerData(LatLng(37.5600, 126.9750), "위험 지역 A: 강남역 부근",
            MapMarkerData.MarkerType.CRIMINAL, "150m"),
        MapMarkerData(LatLng(37.5700, 126.9800), "주의 인물 B: 시청 앞",
            MapMarkerData.MarkerType.CRIMINAL, "300m")
    )
    val sampleSafetyBellMarkers = listOf(
        MapMarkerData(LatLng(37.5650, 126.9700), "안심벨 X: 광화문 광장",
            MapMarkerData.MarkerType.SAFETY_BELL, "200m")
    )

    var sampleSearchText by remember { mutableStateOf("") }

    // MaterialTheme { // 실제 앱의 테마로 감싸주세요
    HomeScreen(
        userLatLng = sampleUserLatLng,
        userAddress = sampleUserAddress,
        userProfileImg = sampleUserProfileImg,
        userProfileName = sampleUserProfileName,
        criminalMarkers = sampleCriminalMarkers,
        safetyBellMarkers = sampleSafetyBellMarkers,
        searchText = sampleSearchText,
        onSearchTextChange = { sampleSearchText = it },
        onSearchClick = { println("Preview Search Clicked: $sampleSearchText") },
        onModalMarkerItemClick = { markerData -> println("Preview Marker Item Clicked: ${markerData.address}") },
        searchedLatLng = null,
        onMsgReportClick = { println("Preview Message Report Clicked") }
    )
    // }
}
