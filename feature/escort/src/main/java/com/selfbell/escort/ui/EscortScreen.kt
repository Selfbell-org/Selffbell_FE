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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.model.Contact
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography

@Composable
fun EscortScreen(
    navController: NavController,
    viewModel: EscortViewModel = hiltViewModel()
) {
    // ViewModel의 모든 상태를 구독합니다.
    val escortFlowState by viewModel.escortFlowState.collectAsState()
    val startLocation by viewModel.startLocation.collectAsState()
    val destinationLocation by viewModel.destinationLocation.collectAsState()
    val arrivalMode by viewModel.arrivalMode.collectAsState()
    val timerMinutes by viewModel.timerMinutes.collectAsState()
    val expectedArrivalTime by viewModel.expectedArrivalTime.collectAsState()
    val isSetupComplete by viewModel.isSetupComplete.collectAsState()
    val isDestinationSelected by viewModel.isDestinationSelected.collectAsState()
    val favoriteAddresses by viewModel.favoriteAddresses.collectAsState()
    val showGuardianShareSheet by viewModel.showGuardianShareSheet.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGuardians by viewModel.selectedGuardians.collectAsState()
    val isSearchingAddress by viewModel.isSearchingAddress.collectAsState()
    val addressSearchQuery by viewModel.addressSearchQuery.collectAsState()
    val addressSearchResults by viewModel.addressSearchResults.collectAsState()
    val selectedAddressForConfirmation by viewModel.selectedAddressForConfirmation.collectAsState()


    val filteredContacts = remember(searchQuery, allContacts) {
        if (searchQuery.isEmpty()) allContacts
        else allContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ReusableNaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPosition = startLocation.latLng,
            onMapReady = { naverMap ->
                // TODO: 출발지, 도착지, 현재위치 마커를 지도에 표시하는 로직
            }
        )

        when (escortFlowState) {
            EscortFlowState.SETUP -> {
                // --- 설정 단계 UI ---
                EscortSetupSheet(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                    startLocationName = "현재 위치",
                    destinationLocationName = destinationLocation.name.takeIf { it != "메인 주소 (더미)" } ?: "선택하기",
                    favoriteAddresses = favoriteAddresses,
                    onFavoriteClick = viewModel::onFavoriteAddressSelected,
                    onDirectInputClick = { navController.navigate(AppRoute.ADDRESS_SEARCH_ROUTE) },
                    isDestinationSelected = isDestinationSelected,
                    arrivalMode = arrivalMode,
                    timerMinutes = timerMinutes,
                    expectedArrivalTime = expectedArrivalTime,
                    onModeChange = viewModel::setArrivalMode,
                    onTimerChange = viewModel::setTimerMinutes,
                    onExpectedArrivalTimeChange = viewModel::setExpectedArrivalTime
                )

                // 하단 '출발하기' 버튼
                SelfBellButton(
                    text = "출발하기",
                    onClick = { viewModel.startSafeWalk() },
                    enabled = isSetupComplete,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.9f)
                )
            }
            EscortFlowState.IN_PROGRESS -> {
                // --- 진행 중 UI ---
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                        )
                    }
                }

                // 하단 '도착 완료' 버튼
                SelfBellButton(
                    text = "도착 완료",
                    onClick = { viewModel.endSafeWalk() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.9f)
                )
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("동선을 공유할 친구를 선택해주세요.", style = Typography.titleMedium)
            // TODO: ShareRouteTopSheet의 검색창, 연락처 목록(LazyColumn) 등 내부 UI 구현 필요
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EscortScreenPreview() {
    SelfBellTheme {
        EscortScreen(navController = rememberNavController())
    }
}