package com.selfbell.home.ui

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
import com.selfbell.core.R as CoreR
import android.util.Log
import com.selfbell.domain.model.CriminalDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMsgReportClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val cameraTargetLatLng by viewModel.cameraTargetLatLng.collectAsState()
    val searchResultMessage by viewModel.searchResultMessage.collectAsState()

    val criminals by viewModel.criminals.collectAsState()

    val selectedCriminalDetail by viewModel.selectedCriminalDetail.collectAsState()
    val selectedEmergencyBellDetail = (uiState as? HomeUiState.Success)?.selectedEmergencyBellDetail


    val mapMarkerMode by viewModel.mapMarkerMode.collectAsState()

    val isCriminalsLoading by viewModel.isCriminalsLoading.collectAsState()

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var infoWindowData by remember { mutableStateOf<Pair<LatLng, String>?>(null) }

    val allMarkers = remember { mutableListOf<Marker>() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val modalMode by remember(selectedEmergencyBellDetail, selectedCriminalDetail) {
        derivedStateOf {
            if (selectedEmergencyBellDetail != null || selectedCriminalDetail != null) {
                ModalMode.DETAIL
            } else {
                ModalMode.SEARCH
            }
        }
    }

    val mapClickModifier = Modifier.clickable {
        if (modalMode == ModalMode.DETAIL) {
            viewModel.clearDetails()
        }
    }

    val floatingBottomPadding = LocalFloatingBottomBarPadding.current

    LaunchedEffect(cameraTargetLatLng) {
        Log.d("HomeScreen", "Camera position changed to $cameraTargetLatLng")
        cameraTargetLatLng?.let { latLng ->
            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing))
        }
    }

    LaunchedEffect(naverMapInstance, uiState, mapMarkerMode) {
        Log.d("HomeScreen", "LaunchedEffect triggered. mapMarkerMode: $mapMarkerMode")
        val map = naverMapInstance ?: return@LaunchedEffect

        Log.d("HomeScreen", "Clearing all markers. Total markers: ${allMarkers.size}")
        allMarkers.forEach { it.map = null }
        allMarkers.clear()
        Log.d("HomeScreen", "All existing markers cleared.")

        if (uiState is HomeUiState.Success) {
            val successState = uiState as HomeUiState.Success

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
                    }
                )
                allMarkers.add(marker)
                Log.d("HomeScreen", "Added emergency bell marker at ${bell.lat}, ${bell.lon}")
            }
            Log.d("HomeScreen", "Added ${successState.emergencyBells.size} emergency bell markers.")

            if (mapMarkerMode == MapMarkerMode.SAFETY_BELL_AND_CRIMINALS) {
                Log.d("HomeScreen", "Attempting to add criminals. Count: ${criminals.size}")
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
                        }
                    )
                    allMarkers.add(marker)
                    Log.d("HomeScreen", "Added criminal marker at ${criminal.lat}, ${criminal.lon}")
                }
                Log.d("HomeScreen", "Added ${criminals.size} criminal markers.")
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize().then(mapClickModifier),
            cameraPosition = cameraTargetLatLng ?: DEFAULT_LAT_LNG,
            onMapReady = { map ->
                naverMapInstance = map
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

                MapMarkerToggleButton(
                    mapMarkerMode = mapMarkerMode,
                    onToggle = { viewModel.toggleMapMarkerMode() },
                    isLoading = isCriminalsLoading,
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
                                "SafeBell 사용자",
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                        }
                        IconButton(
                            onClick = {
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
                    onMarkerItemClick = { markerData ->
                        // 공통 로직: 지도 이동 및 검색창 텍스트 업데이트
                        viewModel.onMapMarkerClicked(markerData)

                        // 상세 정보 표시 로직
                        if (markerData.type == MapMarkerData.MarkerType.SAFETY_BELL && markerData.objtId != null) {
                            viewModel.getEmergencyBellDetail(markerData.objtId)
                        } else if (markerData.type == MapMarkerData.MarkerType.CRIMINAL) {
                            val criminalDetail = CriminalDetail(
                                address = markerData.address,
                                lat = markerData.latLng.latitude,
                                lon = markerData.latLng.longitude,
                                distanceMeters = markerData.distance
                            )
                            viewModel.setSelectedCriminalDetail(criminalDetail)
                        }
                    },
                    selectedEmergencyBellDetail = state.selectedEmergencyBellDetail,
                    selectedCriminalDetail = selectedCriminalDetail,
                    modalMode = modalMode,
                    mapMarkerMode = mapMarkerMode,
                    onDetailDismiss = { viewModel.clearDetails() },
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

private fun addMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (MapMarkerData) -> Unit
): Marker {
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

@Composable
fun MapMarkerToggleButton(
    mapMarkerMode: MapMarkerMode,
    onToggle: () -> Unit,
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