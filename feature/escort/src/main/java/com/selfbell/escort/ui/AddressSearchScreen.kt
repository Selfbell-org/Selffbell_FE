package com.selfbell.escort.ui

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
            TopAppBar(
                title = { Text("도착지 검색", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // 지도 확인 화면에서는 검색 목록으로, 검색 화면에서는 이전 화면으로
                        if (selectedAddress != null) {
                            viewModel.clearConfirmation()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ✅ 상태 변수를 불변의 지역 변수로 복사합니다.
            val currentSelectedAddress = selectedAddress

            if (currentSelectedAddress == null) {
                // --- 1 & 2. 주소 검색 및 결과 목록 UI ---
                AddressSearchView(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onAddressSelect = viewModel::selectAddressForConfirmation
                )
            } else {
                // --- 3. 지도 확인 UI ---
                AddressConfirmView(
                    address = currentSelectedAddress,
                    onConfirm = {
                        // 디버깅 로그: 지도 확인에서 확정 누른 시점
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("이 위치가 맞는지 확인해주세요", style = Typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("주소", style = Typography.labelMedium, color = Color.Gray)
            Text(address.roadAddress.ifEmpty { address.jibunAddress }, style = Typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 남은 공간 모두 차지
                .clip(RoundedCornerShape(12.dp))
        ) {
            ReusableNaverMap(
                cameraPosition = position,
                onMapReady = { map ->
                    naverMap = map
                    marker = moveOrAddMarker(map, position, marker)
                    map.moveCamera(CameraUpdate.scrollTo(position))
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        SelfBellButton(
            text = "도착지 설정",
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
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