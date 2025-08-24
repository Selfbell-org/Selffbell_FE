package com.selfbell.home.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData
import kotlinx.coroutines.launch
import com.naver.maps.map.overlay.OverlayImage
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding
import com.selfbell.alerts.model.AlertData
import com.selfbell.alerts.model.AlertType
import com.selfbell.core.R as CoreR
import android.util.Log // ✅ Log 클래스 임포트
import com.selfbell.domain.model.CriminalDetail

// ✅ 새로운 Enum 클래스를 정의하여 지도에 표시할 마커 모드를 관리합니다.
//enum class MapMarkerMode {
//    SAFETY_BELL_ONLY, // 안심벨만 표시
//    SAFETY_BELL_AND_CRIMINALS // 안심벨 + 범죄자 모두 표시
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // ViewModel을 직접 주입받고, UI는 이를 관찰합니다.
    viewModel: HomeViewModel = hiltViewModel(),
    onMsgReportClick: () -> Unit
) {
    // ViewModel의 상태를 관찰합니다.
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val cameraTargetLatLng by viewModel.cameraTargetLatLng.collectAsState()
    val searchResultMessage by viewModel.searchResultMessage.collectAsState()

    // ✅ 변경: HomeViewModel의 criminals 상태를 직접 관찰합니다.
    val criminals by viewModel.criminals.collectAsState()

    // ✅ 성범죄자 상세 정보 상태 관찰
    val selectedCriminalDetail by viewModel.selectedCriminalDetail.collectAsState()

    // ✅ ViewModel의 새로운 상태를 관찰합니다.
    val mapMarkerMode by viewModel.mapMarkerMode.collectAsState()

    // ✅ 추가: 범죄자 정보 로딩 상태를 관찰합니다.
    val isCriminalsLoading by viewModel.isCriminalsLoading.collectAsState()

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var infoWindowData by remember { mutableStateOf<Pair<LatLng, String>?>(null) }
    var currentModalMode by remember { mutableStateOf(ModalMode.SEARCH) }

    // ✅ 지도에 추가된 마커들을 관리할 상태 추가
    val allMarkers = remember { mutableListOf<Marker>() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val mapClickModifier = Modifier.clickable {
        if (currentModalMode != ModalMode.SEARCH) {
            currentModalMode = ModalMode.SEARCH
            // 상세정보 상태 초기화
            viewModel.setSelectedEmergencyBellDetail(null)
            viewModel.setSelectedCriminalDetail(null)
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "문자 발송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "문자 발송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    // 플로팅 바텀바 패딩 (오버레이 전용)
    val floatingBottomPadding = LocalFloatingBottomBarPadding.current

    // ✅ LaunchedEffect로 카메라 위치 변경 관찰
    LaunchedEffect(cameraTargetLatLng) {
        Log.d("HomeScreen", "Camera position changed to $cameraTargetLatLng")
        cameraTargetLatLng?.let { latLng ->
            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing))
        }
    }

    // ✅ 새로운 LaunchedEffect 추가: 마커 모드와 범죄자 데이터 변경을 관찰하여 마커 업데이트
    // ✅ 수정: 마커 모드, 범죄자 데이터, 맵 인스턴스 변경을 관찰하여 마커 업데이트
    LaunchedEffect(naverMapInstance, uiState, mapMarkerMode) {
        Log.d("HomeScreen", "LaunchedEffect triggered. mapMarkerMode: $mapMarkerMode")
        val map = naverMapInstance ?: return@LaunchedEffect

        // ⚠️ 기존 마커 제거 로직: 모든 마커를 제거하고 다시 그립니다.
        Log.d("HomeScreen", "Clearing all markers. Total markers: ${allMarkers.size}")
        allMarkers.forEach { it.map = null }
        allMarkers.clear()
        Log.d("HomeScreen", "All existing markers cleared.")

        if (uiState is HomeUiState.Success) {
            val successState = uiState as HomeUiState.Success

            // 안심벨 마커 추가 (항상 표시)
            Log.d("HomeScreen", "Attempting to add emergency bells. Count: ${successState.emergencyBells.size}")
            successState.emergencyBells.forEach { bell ->
                val marker = addMarker(
                    naverMap = map,
                    latLng = LatLng(bell.lat, bell.lon),
                    data = MapMarkerData(
                        latLng = LatLng(bell.lat, bell.lon),
                        address = bell.detail,
                        type = MapMarkerData.MarkerType.SAFETY_BELL,
                        distance = bell.distance ?: 0.0,
                        objtId = bell.id
                    ),
                    onClick = { markerData ->
                        viewModel.getEmergencyBellDetail(markerData.objtId!!)
                        currentModalMode = ModalMode.DETAIL
                    }
                )
                allMarkers.add(marker)
                Log.d("HomeScreen", "Added emergency bell marker at ${bell.lat}, ${bell.lon}")
            }
            Log.d("HomeScreen", "Added ${successState.emergencyBells.size} emergency bell markers.")

            // 범죄자 마커 추가 (토글 상태에 따라 표시)
            if (mapMarkerMode == MapMarkerMode.SAFETY_BELL_AND_CRIMINALS) {
                Log.d("HomeScreen", "Attempting to add criminals. Count: ${criminals.size}") // ✅ 수정: successState.criminals 대신 criminals 변수 사용
                criminals.forEach { criminal ->
                    val marker = addMarker(
                        naverMap = map,
                        latLng = LatLng(criminal.lat, criminal.lon),
                        data = MapMarkerData(
                            latLng = LatLng(criminal.lat, criminal.lon),
                            address = criminal.address,
                            type = MapMarkerData.MarkerType.CRIMINAL,
                            distance = criminal.distanceMeters
                        ),
                        onClick = { markerData ->
                            // 성범죄자 상세 정보 생성
                            val criminalDetail = CriminalDetail(
                                address = markerData.address,
                                lat = markerData.latLng.latitude,
                                lon = markerData.latLng.longitude,
                                distanceMeters = markerData.distance
                            )
                            viewModel.setSelectedCriminalDetail(criminalDetail)
                            currentModalMode = ModalMode.DETAIL
                        }
                    )
                    allMarkers.add(marker)
                    Log.d("HomeScreen", "Added criminal marker at ${criminal.lat}, ${criminal.lon}")
                }
                Log.d("HomeScreen", "Added ${criminals.size} criminal markers.") // ✅ 수정
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize().then(mapClickModifier),
            cameraPosition = cameraTargetLatLng ?: DEFAULT_LAT_LNG,
            onMapReady = { map ->
                naverMapInstance = map
                // ⚠️ 마커 그리는 로직은 LaunchedEffect로 이동
            }
        )
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                val userLatLng = state.userLatLng
                val emergencyBells = state.emergencyBells
                // 기존의 val criminals = state.criminals 는 사용하지 않습니다.

                // 실제 안심벨과 범죄자 데이터를 MapMarkerData로 변환
                val modalMapMarkers = remember(emergencyBells, criminals) {
                    val emergencyBellMarkers = emergencyBells.map { bell ->
                        MapMarkerData(
                            latLng = LatLng(bell.lat, bell.lon),
                            address = bell.detail,
                            type = MapMarkerData.MarkerType.SAFETY_BELL,
                            distance = bell.distance ?: 0.0,
                            objtId = bell.id
                        )
                    }

                    val criminalMarkers = criminals.map { criminal ->
                        MapMarkerData(
                            latLng = LatLng(criminal.lat, criminal.lon),
                            address = criminal.address,
                            type = MapMarkerData.MarkerType.CRIMINAL,
                            distance = criminal.distanceMeters
                        )
                    }

                    (emergencyBellMarkers + criminalMarkers).sortedBy { it.distance }
                }

                // ✅ MapMarkerToggleButton의 로직을 변경했습니다.
                MapMarkerToggleButton(
                    mapMarkerMode = mapMarkerMode,
                    onToggle = { viewModel.toggleMapMarkerMode() },
                    isLoading = isCriminalsLoading, // ✅ 로딩 상태 전달
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 40.dp, end = 16.dp)
                )

                infoWindowData?.let { (latLngValue, addressValue) ->
                    MapInfoBalloon(address = addressValue, latLng = latLngValue, onDismissRequest = { infoWindowData = null })
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
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
                            painter = painterResource(R.drawable.usre_profileimg_icon),
                            contentDescription = "프로필",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "사용자",
                                style = Typography.labelSmall.copy(color = Color(0xFF949494))
                            )
                            Text(
                                "SelfBell 사용자",
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                        }
                        IconButton(
                            onClick = {
                                currentModalMode = ModalMode.SEARCH
                                coroutineScope.launch { sheetState.show() }
                                onMsgReportClick()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.msg_report_icon),
                                contentDescription = "메시지 신고",
                                tint = Color.Unspecified
                            )
                        }
                    }
                }

                AddressSearchModal(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(floatingBottomPadding)
                        .padding(bottom = 24.dp),
                    searchText = searchText,
                    onSearchTextChange = viewModel::onSearchTextChanged,
                    onSearchClick = viewModel::onSearchConfirmed,
                    mapMarkers = modalMapMarkers,
                    onMarkerItemClick = viewModel::onMapMarkerClicked,
                    selectedEmergencyBellDetail = state.selectedEmergencyBellDetail,
                    selectedCriminalDetail = state.selectedCriminalDetail,
                    modalMode = currentModalMode,
                    onModalModeChange = { newMode -> currentModalMode = newMode },
                    mapMarkerMode = mapMarkerMode // ✅ 추가된 파라미터
                )
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("오류: ${state.message}")
                }
            }
        }
    }


    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch { sheetState.hide() }
            },
            sheetState = sheetState
        ) {
            MessageReportFlow(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                },
                viewModel = viewModel,
                sendSms = { guardians, message ->
                    Log.d("HomeScreen", "=== sendSms 함수 호출됨 ===")
                    Log.d("HomeScreen", "전달받은 보호자: ${guardians.size}명")
                    guardians.forEachIndexed { index, contact ->
                        Log.d("HomeScreen", "보호자 ${index + 1}: ${contact.name} (ID: ${contact.id}, userId: ${contact.userId}, 전화번호: ${contact.phoneNumber})")
                    }
                    Log.d("HomeScreen", "전달받은 메시지: '$message'")
                    
                    // HomeViewModel의 sendEmergencyAlert 호출
                    Log.d("HomeScreen", "HomeViewModel.sendEmergencyAlert() 호출 시작...")
                    viewModel.sendEmergencyAlert(guardians, message)
                    
                    Log.d("HomeScreen", "sendEmergencyAlert 호출 완료, Toast 표시")
                    Toast.makeText(context, "긴급 상황 신고가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ✅ `clearAllMarkers` 함수와 `addOrUpdateMarker` 함수는 제거하고, 새로운 `addMarker` 함수를 사용합니다.
private fun addMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (MapMarkerData) -> Unit
): Marker { // ✅ Marker 객체를 반환하도록 수정
    return Marker().apply {
        position = latLng
        map = naverMap
        icon = OverlayImage.fromResource(data.getIconResource())
        setOnClickListener {
            onClick(data)
            true
        }
    }
}

// ✅ HomeScren에 특화된 토글 버튼 컴포저블을 새로 만들었습니다.
@Composable
fun MapMarkerToggleButton(
    mapMarkerMode: MapMarkerMode,
    onToggle: () -> Unit,
    // ✅ 추가: 로딩 상태 파라미터
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // ✅ 로딩 상태에 따라 UI 분기
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                painter = painterResource(
                    id = if (mapMarkerMode == MapMarkerMode.SAFETY_BELL_ONLY) {
                        CoreR.drawable.alerts_icon // 안심벨만 표시할 때 아이콘
                    } else {
                        CoreR.drawable.crime_pin_icon // 범죄자도 표시할 때 아이콘
                    }
                ),
                contentDescription = "Map Marker Toggle",
                tint = Color.Unspecified
            )
        }
    }
}