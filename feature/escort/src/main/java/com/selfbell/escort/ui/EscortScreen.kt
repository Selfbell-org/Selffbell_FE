// feature/escort/ui/EscortScreen.kt
package com.selfbell.escort.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.selfbell.core.model.Contact
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@Composable
fun EscortScreen(
    viewModel: EscortViewModel = hiltViewModel()
) {
    val startLocation by viewModel.startLocation.collectAsState()
    val destinationLocation by viewModel.destinationLocation.collectAsState()
    val arrivalMode by viewModel.arrivalMode.collectAsState()
    val timerMinutes by viewModel.timerMinutes.collectAsState()
    val expectedArrivalTime by viewModel.expectedArrivalTime.collectAsState()

    val allContacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    // ✅ ViewModel의 세션 활성화 상태를 구독
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    // ✅ 선택된 보호자들을 구독
    val selectedGuardians by viewModel.selectedGuardians.collectAsState()

    val filteredContacts = remember(searchQuery, allContacts) {
        if (searchQuery.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    var showShareRouteSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = showShareRouteSheet) {
        showShareRouteSheet = false
    }

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
                // 마커 추가 로직
            }
        )

        if (showShareRouteSheet) {
            ShareRouteTopSheet(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                filteredContacts = filteredContacts,
                selectedGuardians = selectedGuardians,
                onGuardianToggle = { contact ->
                    viewModel.toggleGuardianSelection(contact)
                },
                onStartWithGuardians = {
                    viewModel.startSafeWalkWithGuardians()
                    showShareRouteSheet = false
                },
                onCloseClick = { showShareRouteSheet = false }
            )
        }

        if (isSessionActive) {
            EscortingTopBar(
                modifier = Modifier.align(Alignment.TopCenter),
                onShareClick = {
                    showShareRouteSheet = true
                },
                onEndClick = {
                    viewModel.endSafeWalk() // ✅ ViewModel의 종료 함수 호출
                }
            )
        } else if (!showShareRouteSheet) {
            // ✅ 보호자 선택 시트가 표시되지 않을 때만 도착시간 설정 UI 표시
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LocationInputRow(
                            label = "출발지 입력하기",
                            locationName = startLocation.name,
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                        LocationInputRow(
                            label = "도착지 입력하기",
                            locationName = destinationLocation.name,
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ArrivalTimerSection(
                        arrivalMode = arrivalMode,
                        timerMinutes = timerMinutes,
                        expectedArrivalTime = expectedArrivalTime,
                        onModeChange = { viewModel.setArrivalMode(it) },
                        onTimerChange = { viewModel.setTimerMinutes(it) },
                        onExpectedArrivalTimeChange = { viewModel.setExpectedArrivalTime(it) }
                    )
                }
            }

            SelfBellButton(
                text = "보호자 선택하기",
                onClick = {
                    showShareRouteSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(0.9f)
            )
        }
    }
}
@Composable
fun LocationInputRow(
    label: String,
    locationName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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