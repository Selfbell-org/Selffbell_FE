package com.selfbell.alerts.ui

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.selfbell.domain.model.AddressModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val allAlerts by viewModel.allAlerts.collectAsState()
    val selectedAlertType by viewModel.selectedAlertType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 주소 검색 관련 상태
    val addressSearchQuery by viewModel.addressSearchQuery.collectAsState()
    val searchedAddresses by viewModel.searchedAddresses.collectAsState()
    val searchedLocationCameraTarget by viewModel.searchedLocationCameraTarget.collectAsState()
    val searchResultMessage by viewModel.searchResultMessage.collectAsState()
    val isSearchingAddress by viewModel.isSearchingAddress.collectAsState()


    val filteredAlerts = remember(selectedAlertType, allAlerts, searchQuery) {
        allAlerts.filter {
            it.type == selectedAlertType && (searchQuery.isEmpty() || it.address.contains(
                searchQuery,
                ignoreCase = true
            ))
        }
    }

    val userLatLng = LatLng(37.5665, 126.9780)
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var searchedLocationMarker by remember { mutableStateOf<Marker?>(null) }

    // 마커 리스트를 상태로 관리
    val currentMarkers = remember { mutableStateListOf<Marker>() }

    // 지도 준비 및 알림 마커 업데이트
    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(naverMapInstance, filteredAlerts) {
            currentMarkers.forEach { it.map = null }
            currentMarkers.clear()
            naverMapInstance?.let { naverMap ->
                filteredAlerts.forEach { alertData ->
                    val marker = addOrUpdateMarker(naverMap, alertData) { clickedAlert ->
                        naverMap.moveCamera(
                            CameraUpdate.scrollTo(clickedAlert.latLng)
                                .animate(CameraAnimation.Easing)
                        )
                    }
                    currentMarkers.add(marker)
                }
                // 필터링된 알림이 있을 경우 첫번째 알림으로 카메라 이동 (기존 로직 유지)
                if (filteredAlerts.isNotEmpty() && searchedLocationCameraTarget == null) { // 주소 검색 타겟이 없을 때만
                    naverMap.moveCamera(
                        CameraUpdate.scrollTo(filteredAlerts.first().latLng)
                            .animate(CameraAnimation.Easing)
                    )
                } else if (filteredAlerts.isEmpty() && searchedLocationCameraTarget == null) {
                    naverMap.moveCamera(CameraUpdate.scrollTo(userLatLng))
                }
            }
        }

        // 주소 검색 결과로 카메라 이동
        LaunchedEffect(searchedLocationCameraTarget) {
            searchedLocationCameraTarget?.let { latLng ->
                Log.d("AlertAddress", "Moving camera to target: $latLng")
                naverMapInstance?.let { naverMap ->
                    naverMap.moveCamera(
                        CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing)
                    )
                    // 기존 검색 마커 제거
                    searchedLocationMarker?.map = null
                    // 새로운 마커 생성 및 지도에 추가
                    searchedLocationMarker = Marker().apply {
                        position = latLng
                        this.map = naverMap
                        icon =
                            OverlayImage.fromResource(com.selfbell.core.R.drawable.user_marker_icon) // 예시 아이콘 사용
                        setOnClickListener {
                            // 클릭 시 동작 (예: 정보 창 표시)
                            Log.d("AlertsScreen", "Search location marker clicked.")
                            true
                        }
                    }
                }
            } ?: run {
                searchedLocationMarker?.map = null
            }
        }

        // 검색 결과 메시지 표시 (예: SnackBar)
        val scaffoldState =
            remember { SnackbarHostState() } // Material 3 에서는 rememberSnackbarHostState()
        LaunchedEffect(searchResultMessage) {
            searchResultMessage?.let { message ->
                scaffoldState.showSnackbar(message)
                viewModel.onSearchResultMessageConsumed()
            }
        }

        Scaffold( // SnackBarHost를 사용하기 위해 Scaffold로 감쌈
            snackbarHost = { SnackbarHost(scaffoldState) }
        ) { paddingValues ->
            ReusableNaverMap(
                modifier = Modifier.fillMaxSize(), // 지도에 직접 fillMaxSize 적용
                cameraPosition = userLatLng,
                onMapReady = { naverMap ->
                    naverMapInstance = naverMap
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // 다른 UI 컴포넌트들을 감싸는 Box에만 패딩 적용
            ){

                AlertTypeToggleButton(
                    selectedAlertType = selectedAlertType,
                    onToggle = { newType ->
                        viewModel.setAlertType(newType)
                        // viewModel.onAlertSearchQueryChanged("") // 알림 필터 검색어 초기화
                        viewModel.onAddressSearchQueryChanged("") // 주소 검색어도 초기화
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 16.dp, end = 16.dp)
                )

                // AlertsModal을 AddressSearchModal처럼 작동하도록 수정
                // 이제 이 모달은 '알림 필터링 결과' 또는 '주소 검색 결과'를 보여줄 수 있음
                // UI/UX적으로 이를 어떻게 명확히 구분할지 고민 필요 (예: 모달 상단에 탭 또는 상태 텍스트)
                AlertModal( // 컴포저블 이름 변경 고려
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .padding(bottom = 0.dp, start = 16.dp, end = 16.dp),
                    // 알림 필터 관련
                    alertItems = filteredAlerts,
                    onAlertItemClick = { alertData ->
                        naverMapInstance?.moveCamera(
                            CameraUpdate.scrollTo(alertData.latLng)
                                .animate(CameraAnimation.Easing)
                        )
                    },
                    // 주소 검색 관련
                    addressSearchQuery = addressSearchQuery,
                    onAddressSearchQueryChange = { viewModel.onAddressSearchQueryChanged(it) },
                    onAddressSearchConfirmed = { viewModel.searchAddress() }, // 검색 버튼 클릭 시 API 호출
                    searchedAddressItems = searchedAddresses,
                    onSearchedAddressItemClick = { addressModel ->
                        viewModel.onSearchedAddressClicked(addressModel) // ViewModel에서 카메라 타겟 설정
                    },
                    isSearchingAddress = isSearchingAddress,
                    // 현재 모달이 어떤 내용을 보여줘야 하는지 결정하는 로직 필요
                    // 예: addressSearchQuery가 비어있으면 알림 목록, 아니면 주소 검색 결과
                    displayMode = if (addressSearchQuery.isBlank() && searchedAddresses.isEmpty()) DisplayMode.ALERTS else DisplayMode.ADDRESS_SEARCH
                )
            }
        }
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

// AlertsModal을 확장하여 주소 검색 기능도 처리하도록 수정
enum class DisplayMode {
    ALERTS, // 알림 필터 목록 표시
    ADDRESS_SEARCH // 주소 검색 결과 목록 표시
}

@Composable
fun AlertModal(
    modifier: Modifier = Modifier,
    // 알림 필터 관련
    alertItems: List<AlertData>,
    onAlertItemClick: (AlertData) -> Unit,
    // 주소 검색 관련
    addressSearchQuery: String,
    onAddressSearchQueryChange: (String) -> Unit,
    onAddressSearchConfirmed: () -> Unit,
    searchedAddressItems: List<AddressModel>,
    onSearchedAddressItemClick: (AddressModel) -> Unit,
    isSearchingAddress: Boolean,
    displayMode: DisplayMode
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 16.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)) // 배경 약간 더 불투명하게
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 검색 입력창 (항상 표시)
            OutlinedTextField(
                value = addressSearchQuery, // 주소 검색어를 사용
                onValueChange = onAddressSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (displayMode == DisplayMode.ALERTS) "알림 필터링 또는 주소 검색..." else "주소 또는 장소 검색...") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = { // 검색 실행 버튼 (아이콘 버튼으로)
                    if (addressSearchQuery.isNotBlank()) { // 검색어가 있을 때만 표시
                        IconButton(onClick = onAddressSearchConfirmed) {
                            Icon(Icons.Filled.Search, contentDescription = "주소 검색 실행")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                )
            )
            Spacer(Modifier.height(16.dp))

            if (isSearchingAddress) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // 내용 영역: DisplayMode에 따라 다른 리스트 표시
                when (displayMode) {
                    DisplayMode.ALERTS -> {
                        if (alertItems.isEmpty()) {
                            Text(
                                text = "주변에 해당 알림 정보가 없습니다.",
                                style = Typography.bodyMedium,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                items(alertItems, key = { "alert-${it.id}" }) { alert ->
                                    AlertListItem(alert = alert, onClick = { onAlertItemClick(alert) })
                                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                    DisplayMode.ADDRESS_SEARCH -> {
                        if (searchedAddressItems.isEmpty()) {
                            Text(
                                text = "주소 검색 결과가 없습니다.", // 또는 이전 검색 결과 메시지
                                style = Typography.bodyMedium,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp) // 높이 제한
                            ) {
                                items(searchedAddressItems, key = { "addr-${it.roadAddress}-${it.jibunAddress}" }) { address ->
                                    AddressListItem( // 주소 표시에 맞는 아이템 Composable
                                        address = address,
                                        onClick = { Log.d("AlertsModal", "AddressListItem clicked: ${address.roadAddress}") // 로그 추가
                                            onSearchedAddressItemClick(address) }
                                    )
                                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 주소 검색 결과를 위한 리스트 아이템 Composable (새로 추가 또는 기존 것 활용)
@Composable
fun AddressListItem(address: AddressModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 주소 아이콘 (선택 사항)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = address.roadAddress.ifEmpty { address.jibunAddress }, // 도로명 우선, 없으면 지번
                style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2, // 주소가 길 수 있으므로 2줄 허용
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (address.roadAddress.isNotEmpty() && address.jibunAddress.isNotEmpty()) {
                Text( // 도로명 주소가 있을 경우, 지번 주소를 부가 정보로 표시
                    text = "[지번] ${address.jibunAddress}",
                    style = Typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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