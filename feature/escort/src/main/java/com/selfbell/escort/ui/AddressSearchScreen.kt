package com.selfbell.escort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.moveOrAddMarker
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.AddressModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSearchScreen(
    navController: NavController,
    onAddressSelected: (String, Double, Double) -> Unit,
    viewModel: AddressSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedAddress by viewModel.selectedAddressForConfirmation.collectAsState()

    Scaffold(
        topBar = {
            // TopAppBar 대신 커스텀 Row를 사용하여 공간을 줄였습니다.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // 원하는 높이로 직접 설정
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (selectedAddress != null) {
                        viewModel.clearConfirmation()
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("도착지 검색", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val currentSelectedAddress = selectedAddress

            if (currentSelectedAddress == null) {
                AddressSearchView(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onAddressSelect = viewModel::selectAddressForConfirmation
                )
            } else {
                AddressConfirmView(
                    address = currentSelectedAddress,
                    onConfirm = {
                        android.util.Log.d(
                            "AddressSearch",
                            "AddressConfirmView onConfirm: address=" +
                                    currentSelectedAddress.roadAddress.ifEmpty { currentSelectedAddress.jibunAddress } +
                                    ", y(lat)=" + currentSelectedAddress.y + ", x(lon)=" + currentSelectedAddress.x
                        )
                        onAddressSelected(
                            currentSelectedAddress.roadAddress.ifEmpty { currentSelectedAddress.jibunAddress },
                            currentSelectedAddress.y.toDouble(),
                            currentSelectedAddress.x.toDouble()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AddressSearchView(
    searchQuery: String,
    searchResults: List<AddressModel>,
    onQueryChange: (String) -> Unit,
    onAddressSelect: (AddressModel) -> Unit
) {
    Column {
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Text") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (searchQuery.isBlank()) {
            Text("이렇게 검색해 보세요", style = Typography.bodyMedium)
            Text("・ 도로명 + 건물번호 (예: 위례성대로 2)", style = Typography.bodySmall, color = Color.Gray)
            Text("・ 건물명 + 번지 (예: 방이동 44-2)", style = Typography.bodySmall, color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(searchResults) { address ->
                    AddressResultItem(address = address, onClick = { onAddressSelect(address) })
                }
            }
        }
    }
}
@Composable
private fun AddressConfirmView(
    address: AddressModel,
    onConfirm: () -> Unit
) {
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    val position = LatLng(address.y.toDouble(), address.x.toDouble())

    Column(
        modifier = Modifier.fillMaxSize(), // 👈 Column이 화면 전체를 차지하도록 합니다.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 정보 영역
        Spacer(modifier = Modifier.height(12.dp))
        Text("이 위치가 맞는지 확인해주세요", style = Typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("주소", style = Typography.labelMedium, color = Color.Gray)
            Text(address.roadAddress.ifEmpty { address.jibunAddress }, style = Typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 지도가 남은 공간을 모두 채우도록 weight(1f) 적용
        Box(
            modifier = Modifier
                .fillMaxSize() // 👈 Box가 부모의 남은 공간을 모두 차지하도록 합니다.
                .clip(RoundedCornerShape(12.dp))
        ) {
            // 1. ReusableNaverMap을 첫 번째 자식으로 배치하여 배경이 되게 합니다.
            ReusableNaverMap(
                cameraPosition = position,
                onMapReady = { map ->
                    naverMap = map
                    marker = moveOrAddMarker(map, position, marker)
                    map.moveCamera(CameraUpdate.scrollTo(position))
                }
            )

            // 2. SelfBellButton을 그 위에 배치하고, 정렬 및 위치를 조정합니다.
            SelfBellButton(
                text = "도착지 설정",
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center) // 👈 지도의 수평/수직 중앙에 위치시킵니다.
                    .offset(y = 180.dp) // 👈 중앙에서 아래로 60dp만큼 이동시킵니다.
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}

@Composable
private fun AddressResultItem(address: AddressModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(address.roadAddress, style = Typography.bodyMedium)
        Text(address.jibunAddress, style = Typography.labelMedium, color = Color.Gray)
    }
}