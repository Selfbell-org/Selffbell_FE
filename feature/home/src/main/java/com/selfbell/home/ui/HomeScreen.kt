package com.selfbell.home.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Use material3 components
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

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var infoWindowData by remember { mutableStateOf<Pair<LatLng, String>?>(null) }
    var currentModalMode by remember { mutableStateOf(ModalMode.SEARCH) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val mapClickModifier = Modifier.clickable {
        if (currentModalMode != ModalMode.SEARCH) {
            currentModalMode = ModalMode.SEARCH
            // 상세정보 상태 초기화
            viewModel.setSelectedEmergencyBellDetail(null)
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

    LaunchedEffect(cameraTargetLatLng) {
        cameraTargetLatLng?.let { latLng ->
            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing))
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                val userLatLng = state.userLatLng
                val emergencyBells = state.emergencyBells
                val criminals = state.criminals
                
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

                ReusableNaverMap(
                    modifier = Modifier.fillMaxSize().then(mapClickModifier), // ✅ 지도에 클릭 모디파이어 적용,
                    cameraPosition = cameraTargetLatLng ?: DEFAULT_LAT_LNG,
                    onMapReady = { map ->
                        naverMapInstance = map
                        
                        // ✅ 안심벨 마커를 지도에 추가하는 로직
                        emergencyBells.forEach { bell ->
                            addOrUpdateMarker(
                                naverMap = map,
                                latLng = LatLng(bell.lat, bell.lon),
                                data = MapMarkerData(
                                    latLng = LatLng(bell.lat, bell.lon),
                                    address = bell.detail, // ins_DETAIL을 주소로 사용
                                    type = MapMarkerData.MarkerType.SAFETY_BELL, // 안심벨 타입
                                    distance = bell.distance ?: 0.0, // null인 경우 0.0으로 설정
                                    objtId = bell.id
                                ),
                                onClick = { markerData ->
                                    // 안심벨 마커 클릭 시 상세정보 API 호출
                                    viewModel.getEmergencyBellDetail(markerData.objtId!!)
                                    // 모달 모드를 상세 정보 보기로 전환
                                    currentModalMode = ModalMode.DETAIL
                                }
                            )
                        }
                        
                        // ✅ 범죄자 마커를 지도에 추가하는 로직
                        criminals.forEach { criminal ->
                            addOrUpdateMarker(
                                naverMap = map,
                                latLng = LatLng(criminal.lat, criminal.lon),
                                data = MapMarkerData(
                                    latLng = LatLng(criminal.lat, criminal.lon),
                                    address = criminal.address,
                                    type = MapMarkerData.MarkerType.CRIMINAL,
                                    distance = criminal.distanceMeters
                                ),
                                onClick = { markerData ->
                                    infoWindowData = markerData.latLng to markerData.address
                                }
                            )
                        }
                    }
                )

                infoWindowData?.let { (latLngValue, addressValue) ->
                    MapInfoBalloon(address = addressValue, latLng = latLngValue, onDismissRequest = { infoWindowData = null })
                }

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

                AddressSearchModal(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    searchText = searchText,
                    onSearchTextChange = viewModel::onSearchTextChanged,
                    onSearchClick = viewModel::onSearchConfirmed,
                    mapMarkers = modalMapMarkers,
                    onMarkerItemClick = viewModel::onMapMarkerClicked,
                    // ✅ ViewModel에서 가져온 상세정보를 모달에 전달
                    selectedEmergencyBellDetail = state.selectedEmergencyBellDetail,
                    // ✅ 모달의 현재 모드 전달
                    modalMode = currentModalMode,
                    // ✅ 모달 모드 변경 콜백 추가
                    onModalModeChange = { newMode -> currentModalMode = newMode }
                )
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("오류: ${state.message}")
                }
            }
        }
    }

//    val smsPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            Toast.makeText(context, "문자 발송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "문자 발송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }

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
                sendSms = { guardians, message ->
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.getSystemService(SmsManager::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                SmsManager.getDefault()
                            }
                            guardians.forEach { contact ->
                                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                            }
                            Toast.makeText(context, "긴급 문자가 발송되었습니다.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "문자 발송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    } else {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                }
            )
        }
    }
}


fun addOrUpdateMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (MapMarkerData) -> Unit
) {
    Marker().apply {
        position = latLng
        map = naverMap
        icon = OverlayImage.fromResource(data.getIconResource())
        setOnClickListener {
            onClick(data) // ✅ data 객체 전체를 전달
            true
        }
    }
}