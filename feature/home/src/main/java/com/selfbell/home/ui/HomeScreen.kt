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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData
import kotlinx.coroutines.launch
import com.naver.maps.map.overlay.OverlayImage
import com.selfbell.core.ui.theme.Primary

// Removed @OptIn(ExperimentalMaterialApi::class) as it's for the old library.
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Use the correct material3 API
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        // The initial value is now part of the `ModalBottomSheet` composable itself.
    )

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "문자 발송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "문자 발송 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val modalMapMarkers = remember(criminalMarkers, safetyBellMarkers) {
        (criminalMarkers + safetyBellMarkers).sortedBy { it.distance }
    }

    // Replace ModalBottomSheetLayout with a conditional ModalBottomSheet
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
                    Text(
                        "profile",
                        style = Typography.labelSmall.copy(color = Color(0xFF949494))
                    )
                    Text(
                        userProfileName,
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
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

    // Use a conditional statement to show the material3 ModalBottomSheet
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
    onClick: (Pair<LatLng, String>) -> Unit
) {
    Marker().apply {
        position = latLng
        map = naverMap
        icon = OverlayImage.fromResource(data.getIconResource())
        setOnClickListener {
            onClick(latLng to data.address)
            true
        }
    }
}