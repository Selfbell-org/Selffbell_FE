package com.selfbell.alerts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
import com.selfbell.alerts.model.AlertData
import com.selfbell.alerts.model.AlertType
import com.selfbell.core.R
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextOverflow
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val allAlerts by viewModel.allAlerts.collectAsState()
    val selectedAlertType by viewModel.selectedAlertType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredAlerts = remember(selectedAlertType, allAlerts, searchQuery) {
        allAlerts.filter {
            it.type == selectedAlertType && (searchQuery.isEmpty() || it.address.contains(searchQuery, ignoreCase = true))
        }
    }

    val userLatLng = LatLng(37.5665, 126.9780)
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var cameraPosition by remember { mutableStateOf(userLatLng) } // 카메라 위치 상태 변수 추가

    // 마커 리스트를 상태로 관리
    val currentMarkers = remember { mutableStateListOf<Marker>() }

    // 지도가 준비되거나 필터링된 알림이 변경될 때마다 마커를 업데이트
    LaunchedEffect(naverMapInstance, filteredAlerts) {
        currentMarkers.forEach { it.map = null }
        currentMarkers.clear()

        naverMapInstance?.let { naverMap ->
            filteredAlerts.forEach { alertData ->
                val marker = addOrUpdateMarker(naverMap, alertData) {
                    // TODO: 마커 클릭 시 동작
                }
                currentMarkers.add(marker)
            }
        }
    }

    // 카메라 위치 변경 로직
    LaunchedEffect(cameraPosition) {
        naverMapInstance?.moveCamera(CameraUpdate.scrollTo(cameraPosition))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = userLatLng, // 초기 카메라 위치
            onMapReady = { naverMap ->
                naverMapInstance = naverMap
            }
        )

        // 우측 상단 필터 토글 버튼
        AlertTypeToggleButton(
            selectedAlertType = selectedAlertType,
            onToggle = { viewModel.setAlertType(it) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        )

        // 하단 알림 리스트 (AddressSearchModal과 유사한 레이아웃)
        AlertsModal(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            filteredAlerts = filteredAlerts,
            searchText = searchQuery,
            onSearchTextChange = { viewModel.setSearchQuery(it) },
            onSearchClick = { /* TODO: 검색 버튼 클릭 로직 */ },
            onItemClick = { alertData -> // 리스트 아이템 클릭 시 카메라 위치 변경
                naverMapInstance?.moveCamera(CameraUpdate.scrollTo(alertData.latLng).animate(
                    CameraAnimation.Easing))
            }
        )
    }
}

fun addOrUpdateMarker(
    naverMap: NaverMap,
    alertData: AlertData,
    onClick: (AlertData) -> Unit
): Marker {
    return Marker().apply {
        position = alertData.latLng
        map = naverMap
        icon = OverlayImage.fromResource(alertData.getIconResource())
        setOnClickListener {
            onClick(alertData)
            true
        }
    }
}

// 필터 버튼 (토글)
@Composable
fun AlertTypeToggleButton(
    selectedAlertType: AlertType,
    onToggle: (AlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            val newType = if (selectedAlertType == AlertType.CRIMINAL_INFO) {
                AlertType.EMERGENCY_CALL
            } else {
                AlertType.CRIMINAL_INFO
            }
            onToggle(newType)
        },
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        Icon(
            painter = painterResource(
                id = if (selectedAlertType == AlertType.EMERGENCY_CALL) {
                    R.drawable.alerts_icon
                } else {
                    R.drawable.crime_pin_icon
                }
            ),
            contentDescription = "Alert Type Toggle",
            tint = Color.Unspecified
        )
    }
}

// AddressSearchModal과 유사한 레이아웃의 AlertsModal 컴포저블
@Composable
fun AlertsModal(
    modifier: Modifier = Modifier,
    filteredAlerts: List<AlertData>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onItemClick: (AlertData) -> Unit // 파라미터 추가
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.93f)
            .wrapContentHeight()
            .shadow(
                elevation = 16.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xCCFFFFFF))
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // 검색 입력창
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = { Text("내 주변 탐색") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearchClick() }
                )
            }
            Spacer(Modifier.height(18.dp))

            // 알림 리스트 (LazyColumn)
            if (filteredAlerts.isEmpty()) {
                Text(
                    text = "주변에 해당 정보가 없습니다.",
                    style = Typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                // LazyColumn이 Card의 남은 공간을 차지하도록 weight(1f)를 사용
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                ) {
                    items(filteredAlerts) { alert ->
                        AlertListItem(
                            alert = alert,
                            onClick = { onItemClick(it) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

// AlertListItem 컴포저블 수정
@Composable
fun AlertListItem(alert: AlertData, onClick: (AlertData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onClick(alert) }, // clickable에서 콜백 호출
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconRes = if (alert.type == AlertType.EMERGENCY_CALL) {
            R.drawable.alerts_icon
        } else {
            R.drawable.crime_pin_icon
        }
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = alert.address,
            tint = Color.Unspecified,
            modifier = Modifier.size(38.dp)
        )

        Text(
            alert.address,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text("${alert.distance}m", style = Typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    SelfBellTheme {
        AlertsScreen()
    }
}