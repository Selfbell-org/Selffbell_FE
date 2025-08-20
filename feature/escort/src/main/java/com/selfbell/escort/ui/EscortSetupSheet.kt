package com.selfbell.escort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.FavoriteAddress
import java.time.LocalTime

@Composable
fun EscortSetupSheet(
    modifier: Modifier = Modifier,
    startLocationName: String,
    destinationLocationName: String,
    favoriteAddresses: List<FavoriteAddress>,
    onFavoriteClick: (FavoriteAddress) -> Unit,
    onDirectInputClick: () -> Unit,
    isDestinationSelected: Boolean,
    arrivalMode: ArrivalMode,
    timerMinutes: Int,
    expectedArrivalTime: LocalTime?,
    onModeChange: (ArrivalMode) -> Unit,
    onTimerChange: (Int) -> Unit,
    onExpectedArrivalTimeChange: (LocalTime) -> Unit
) {
    var activeTab by remember { mutableStateOf("destination") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- 목적지 설정 섹션 ---
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton(
                    text = "출발지 입력",
                    subText = startLocationName,
                    isSelected = activeTab == "start",
                    onClick = { activeTab = "start" },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(
                    text = "도착지 입력",
                    subText = destinationLocationName,
                    isSelected = activeTab == "destination",
                    onClick = { activeTab = "destination" },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text("즐겨찾기", style = Typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(favoriteAddresses) { address ->
                    FavoriteButton(
                        text = address.name,
                        // ✅ 현재 선택된 목적지 이름과 버튼의 이름이 같으면 isSelected = true
                        isSelected = (destinationLocationName == address.name),
                        onClick = { onFavoriteClick(address) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ '직접 입력' 버튼의 텍스트와 스타일을 isDestinationSelected 상태에 따라 변경
            val isFavoriteSelected = favoriteAddresses.any { it.name == destinationLocationName }
            Button(
                onClick = onDirectInputClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    // 직접 입력으로 주소가 선택되었을 때(즐겨찾기 선택이 아닐 때) 활성화 색상 표시
                    containerColor = if (isDestinationSelected && !isFavoriteSelected) Primary else Color(0xFFF5F5F5)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (isDestinationSelected && !isFavoriteSelected) {
                        // ✅ 직접 입력으로 선택된 경우: 버튼 라벨을 설정한 주소로 표시
                        Text(destinationLocationName, color = Color.White)
                    } else {
                        Text("도착지 주소 직접 입력..", color = Color.Gray)
                    }
                }
            }

            // --- 도착 시간 설정 섹션 ---
            AnimatedVisibility(visible = isDestinationSelected) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    ArrivalTimerSection(
                        arrivalMode = arrivalMode,
                        timerMinutes = timerMinutes,
                        expectedArrivalTime = expectedArrivalTime,
                        onModeChange = onModeChange,
                        onTimerChange = onTimerChange,
                        onExpectedArrivalTimeChange = onExpectedArrivalTimeChange
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    subText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color.White else Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Text(
                text = subText,
                style = Typography.bodyMedium,
                color = Color.White
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if(text == "출발지 입력") Color.Green else Color.Red, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subText,
                    style = Typography.bodyMedium,
                    color = Black
                )
            }
        }
    }
}

@Composable
private fun FavoriteButton(
    text: String, onClick: () -> Unit,
    isSelected: Boolean) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        // ✅ isSelected 값에 따라 버튼 색상을 다르게 표시
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Primary else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Black
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text)
    }
}