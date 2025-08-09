// feature/escort/ui/EscortScreen.kt
package com.selfbell.escort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Black

@Composable
fun EscortScreen(
    viewModel: EscortViewModel = hiltViewModel()
) {
    val startLocation by viewModel.startLocation.collectAsState()
    val destinationLocation by viewModel.destinationLocation.collectAsState()
    val arrivalMode by viewModel.arrivalMode.collectAsState()
    val timerMinutes by viewModel.timerMinutes.collectAsState()

    val mapCenter = remember(startLocation, destinationLocation) {
        val lat = (startLocation.latLng.latitude + destinationLocation.latLng.latitude) / 2
        val lng = (startLocation.latLng.longitude + destinationLocation.latLng.longitude) / 2
        LatLng(lat, lng)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = mapCenter,
            onMapReady = { naverMap ->
                // 마커 추가 로직 (출발지, 도착지)
            }
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .fillMaxWidth(0.9f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 출발지/도착지 입력 UI를 가로로 배치
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // 두 칸 사이의 간격
                ) {
                    // 출발지 입력
                    LocationInputRow(
                        label = "출발지 입력하기",
                        locationName = startLocation.name,
                        onClick = { /* TODO: 출발지 주소 검색 화면으로 이동 */ },
                        modifier = Modifier.weight(1f)
                    )
                    // 도착지 입력
                    LocationInputRow(
                        label = "도착지 입력하기",
                        locationName = destinationLocation.name,
                        onClick = { /* TODO: 도착지 주소 검색 화면으로 이동 */ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 도착 시간 설정
                ArrivalTimerSection(
                    arrivalMode = arrivalMode,
                    timerMinutes = timerMinutes,
                    onModeChange = { viewModel.setArrivalMode(it) },
                    onTimerChange = { viewModel.setTimerMinutes(it) }
                )
            }
        }

        // 하단 '출발' 버튼
        SelfBellButton(
            text = "출발",
            onClick = { /* TODO: 안심귀가 서비스 시작 */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun LocationInputRow(
    label: String,
    locationName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // modifier를 추가하여 부모 컴포넌트에서 제어 가능하게 함
) {
    Row(
        modifier = modifier // modifier를 적용
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = Typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = locationName, style = Typography.bodyMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EscortScreenPreview() {
    SelfBellTheme {
        EscortScreen()
    }
}