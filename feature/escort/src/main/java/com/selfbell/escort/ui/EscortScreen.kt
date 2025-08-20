// feature/escort/ui/EscortScreen.kt
package com.selfbell.escort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
import com.selfbell.core.model.Contact
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography

@Composable
fun EscortScreen(
    viewModel: EscortViewModel = hiltViewModel()
) {
    // ViewModel의 모든 상태를 구독합니다.
    val escortFlowState by viewModel.escortFlowState.collectAsState()
    val startLocation by viewModel.startLocation.collectAsState()
    val destinationLocation by viewModel.destinationLocation.collectAsState()
    val arrivalMode by viewModel.arrivalMode.collectAsState()
    val timerMinutes by viewModel.timerMinutes.collectAsState()
    val expectedArrivalTime by viewModel.expectedArrivalTime.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGuardians by viewModel.selectedGuardians.collectAsState()
    val favoriteAddresses by viewModel.favoriteAddresses.collectAsState()

    // '출발하기' 버튼 활성화 여부 상태
    val isSetupComplete by viewModel.isSetupComplete.collectAsState()
    // 세션 진행 중 보호자 공유 UI 표시 여부 상태
    val showGuardianShareSheet by viewModel.showGuardianShareSheet.collectAsState()

    val filteredContacts = remember(searchQuery, allContacts) {
        if (searchQuery.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = startLocation.latLng,
            onMapReady = { naverMap ->
                // TODO: 출발지, 도착지, 현재위치 마커를 지도에 표시하는 로직
            }
        )

        // --- 상단 UI 그룹 ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (escortFlowState) {
                EscortFlowState.SETTING_DESTINATION, EscortFlowState.SETTING_ARRIVAL_TIME -> {
                    // 1단계: 목적지 설정
                    DestinationSetupSheet(
                        startLocationName = "현재 위치",
                        destinationLocationName = destinationLocation.name.takeIf { it != "메인 주소 (더미)" } ?: "선택하기",
                        favoriteAddresses = favoriteAddresses,
                        onFavoriteClick = { address -> viewModel.onFavoriteAddressSelected(address) },
                        onDirectInputClick = { /* TODO: 주소 검색 화면으로 이동하는 로직 */ },
                        onConfirmClick = { viewModel.onDestinationSet() } // 다음 단계로 이동
                    )
                }
                EscortFlowState.IN_PROGRESS -> {
                    // 4단계: 안심귀가 진행 중
                    EscortingInfoCard(
                        destinationName = destinationLocation.name,
                        onShareClick = { viewModel.toggleGuardianShareSheet() }
                    )

                    if (showGuardianShareSheet) {
                        GuardianShareSheet(
                            searchQuery = searchQuery,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            filteredContacts = filteredContacts,
                            selectedGuardians = selectedGuardians,
                            onGuardianToggle = viewModel::toggleGuardianSelection,
                            onCloseClick = { viewModel.toggleGuardianShareSheet() }
                            // TODO: 공유 완료 버튼 및 로직 추가
                        )
                    }
                }
                else -> {}
            }
        }

        // --- 하단 UI 그룹 ---
        when (escortFlowState) {
            EscortFlowState.SETTING_ARRIVAL_TIME -> {
                // 2단계: 도착 시간 설정 UI
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp) // 출발 버튼 위로 배치
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    ArrivalTimerSection(
                        arrivalMode = arrivalMode,
                        timerMinutes = timerMinutes,
                        expectedArrivalTime = expectedArrivalTime,
                        onModeChange = viewModel::setArrivalMode,
                        onTimerChange = viewModel::setTimerMinutes,
                        onExpectedArrivalTimeChange = viewModel::setExpectedArrivalTime
                    )
                }

                SelfBellButton(
                    text = "출발하기",
                    onClick = { viewModel.startSafeWalk() },
                    enabled = isSetupComplete, // ✅ isSetupComplete 상태에 따라 활성화
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.9f)
                )
            }
            EscortFlowState.IN_PROGRESS -> {
                SelfBellButton(
                    text = "도착 완료",
                    onClick = { viewModel.endSafeWalk() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.9f)
                )
            }
            else -> {}
        }
    }
}


// --- EscortScreen에서 사용하는 새로운 Composable 함수들 ---

@Composable
fun EscortingInfoCard(
    destinationName: String,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("안심귀가를 시작했어요!", style = Typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("목적지: $destinationName", style = Typography.bodyMedium)
            }
            Button(onClick = onShareClick) {
                Text("공유")
            }
        }
    }
}

@Composable
fun GuardianShareSheet(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredContacts: List<Contact>,
    selectedGuardians: Set<Contact>,
    onGuardianToggle: (Contact) -> Unit,
    onCloseClick: () -> Unit
) {
    // 기존 ShareRouteTopSheet의 로직을 재활용하여 카드 형태로 만듭니다.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // ShareRouteTopSheet의 Column 내부 로직과 거의 동일하게 구현
        Column(modifier = Modifier.padding(16.dp)) {
            Text("동선을 공유할 친구를 선택해주세요.", style = Typography.titleMedium)
            // ... 검색창, 연락처 목록 (LazyColumn) 등 ...
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