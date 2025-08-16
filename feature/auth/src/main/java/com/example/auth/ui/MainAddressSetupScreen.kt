package com.example.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.moveOrAddMarker

@Composable
fun MainAddressSetupScreen(
    navController: NavController,
    viewModel: MainAddressSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var marker by remember { mutableStateOf<Marker?>(null) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    Box(Modifier.fillMaxSize()) {
        // 지도 Composable
        ReusableNaverMap(
            modifier = Modifier.matchParentSize(),
            onMapReady = { map ->
                naverMap = map
                uiState.userLatLng?.let { pos ->
                    marker = moveOrAddMarker(map, pos, marker)
                    map.moveCamera(CameraUpdate.scrollTo(pos))
                }
            },
            onLocationChanged = { pos ->
                viewModel.updateUserLatLng(pos) // 지도가 움직일 때마다 ViewModel 업데이트
                naverMap?.let { map ->
                    marker = moveOrAddMarker(map, pos, marker)
                }
            }
        )

        // == 상단 카드 오버레이 UI ==
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .fillMaxWidth(0.95f)
                .defaultMinSize(minHeight = 120.dp),
            colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.97f))
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("메인주소 설정하기", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = { viewModel.updateAddress(it)},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("주소를 입력해 주세요") }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setMainAddress()},
                    enabled = uiState.address.isNotBlank() && uiState.userLatLng != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("설정")
                }
            }
        }

        // == 지도 중앙 현위치 버튼 ==
        Button(
            onClick = {
                naverMap?.locationTrackingMode = LocationTrackingMode.Follow
            },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("현위치")
        }
    }
}


