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

    val allContacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredContacts = remember(searchQuery, allContacts) {
        if (searchQuery.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    var showShareRouteSheet by remember { mutableStateOf(false) }
    var isEscorting by remember { mutableStateOf(false) }

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
                // contact를 받는 onShareClick 람다 전달
                onShareClick = { contact ->
                    println("동선 공유: ${contact.name}")
                    // 창을 닫지 않고 상태 업데이트
                },
                onCloseClick = { showShareRouteSheet = false }
            )
        }

        if (isEscorting) {
            EscortingTopBar(
                modifier = Modifier.align(Alignment.TopCenter),
                onShareClick = {
                    showShareRouteSheet = true
                }
            )
        } else {
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
                        onModeChange = { viewModel.setArrivalMode(it) },
                        onTimerChange = { viewModel.setTimerMinutes(it) }
                    )
                }
            }

            SelfBellButton(
                text = "출발",
                onClick = {
                    isEscorting = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(0.9f)
            )
        }
    }
}

// LocationInputRow 등 나머지 컴포저블 코드
// ...

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