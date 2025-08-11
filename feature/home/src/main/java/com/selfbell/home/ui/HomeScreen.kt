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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.MessageReportBottomSheet
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData
import kotlinx.coroutines.launch
import com.naver.maps.map.overlay.OverlayImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userLatLng: LatLng,
    userAddress: String,
    userProfileImg: Int,
    userProfileName: String,
    criminalMarkers: List<MapMarkerData>,
    safetyBellMarkers: List<MapMarkerData>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onModalMarkerItemClick: (MapMarkerData) -> Unit,
    searchedLatLng: LatLng?,
    onMsgReportClick: () -> Unit // 이 파라미터는 이제 사용하지 않으므로 무시해도 됩니다.
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var showMessageReportModal by remember { mutableStateOf(false) }

    val dummyGuardians = remember {
        listOf(
            Contact(1, "나는돌맹이", "010-1234-5678"),
            Contact(2, "또로리", "010-9876-5432")
        )
    }
    val dummyMessageTemplates = remember {
        listOf(
            "위급 상황입니다.",
            "갇혔어요.",
            "위협 받고 있어요.",
            "누군가 따라오는 것 같아요."
        )
    }

    // SMS 발송 권한 요청 및 처리
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "문자 발송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            // 권한 허용 후 바텀 시트를 다시 보여줘서 '보내기'를 다시 누르도록 유도할 수 있습니다.
            // 여기서는 바텀 시트의 상태를 직접 관리하는 방식으로 구현했습니다.
        } else {
            Toast.makeText(context, "문자 발송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val sendSms = { guardians: List<Contact>, message: String ->
        // SMS 권한이 있는지 확인
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
            coroutineScope.launch { sheetState.hide() }
            showMessageReportModal = false
        } else {
            // 권한이 없으면 요청
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    val modalMapMarkers = remember(criminalMarkers, safetyBellMarkers) {
        (criminalMarkers + safetyBellMarkers).sortedBy { it.distance }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (showMessageReportModal) {
                MessageReportBottomSheet(
                    selectedGuardians = dummyGuardians,
                    messageTemplates = dummyMessageTemplates,
                    onSendClick = { guardians, message -> sendSms(guardians, message) },
                    onCancelClick = {
                        coroutineScope.launch { sheetState.hide() }
                        showMessageReportModal = false
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        },
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
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
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("profile", style = Typography.labelSmall, color = Color(0xFF949494))
                        Text(userProfileName, style = Typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            showMessageReportModal = true
                            coroutineScope.launch {
                                sheetState.show()
                            }
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
                    .padding(bottom = 24.dp),
                searchText = searchText,
                onSearchTextChange = onSearchTextChange,
                onSearchClick = onSearchClick,
                mapMarkers = modalMapMarkers,
                onMarkerItemClick = { markerData ->
                    naverMapInstance?.moveCamera(
                        CameraUpdate.scrollTo(markerData.latLng).animate(
                            CameraAnimation.Easing
                        )
                    )
                }
                    )
        }
    }
}


//// MapInfoBalloon Composable (별도 파일 또는 HomeScreen 하단에 정의)
//@Composable
//fun MapInfoBalloon(
//    modifier: Modifier = Modifier,
//    address: String,
//    latLng: LatLng, // 마커 위치 정보 (필요시 사용)
//    onDismissRequest: () -> Unit
//) {
//    // Popup을 사용하여 지도 위에 오버레이 형태로 표시
//    // Popup의 위치는 직접 계산하거나, Alignment를 사용할 수 있습니다.
//    // 여기서는 간단하게 화면 중앙 근처에 나타나도록 합니다. (실제로는 마커 위치 기반으로 조정 필요)
//    Popup(
//        alignment = Alignment.Center, // 또는 다른 정렬, offset 사용 가능
//        onDismissRequest = onDismissRequest
//    ) {
//        Surface(
//            modifier = modifier
//                .wrapContentSize()
//                .shadow(4.dp, RoundedCornerShape(8.dp))
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color.White)
//                .clickable(onClick = onDismissRequest), // 말풍선 클릭 시 닫기
//            color = Color.White // Surface 자체 색상
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(text = "주소", style = Typography.labelSmall)
//                Text(text = address, style = Typography.bodyLarge)
//                Spacer(modifier = Modifier.height(8.dp))
//                // 필요시 추가 정보 (예: "상세보기 버튼 등")
//            }
//        }
//    }
//}

fun addOrUpdateMarker(
    naverMap: NaverMap,
    latLng: LatLng,
    data: MapMarkerData,
    onClick: (Pair<LatLng, String>) -> Unit
) {
    Marker().apply {
        position = latLng
        map = naverMap
        // 아이콘 리소스를 OverlayImage로 설정
        icon = OverlayImage.fromResource(data.getIconResource())
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
        sampleUserProfileName,
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