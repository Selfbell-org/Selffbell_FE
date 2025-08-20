package com.selfbell.escort.ui


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
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.theme.Black
import com.selfbell.domain.model.FavoriteAddress

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DestinationSetupSheet(
    modifier: Modifier = Modifier,
    startLocationName: String,
    destinationLocationName: String,
    favoriteAddresses: List<FavoriteAddress>, // ✅ 즐겨찾기 목록을 파라미터로 받음
    onFavoriteClick: (FavoriteAddress) -> Unit, // ✅ 클릭 시 객체 전체를 전달
    onDirectInputClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    var activeTab by remember { mutableStateOf("destination") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 탭 버튼 (출발지/도착지)
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

            // ✅ 즐겨찾기 버튼: FlowRow를 LazyRow로 변경
            Text("즐겨찾기", style = Typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp) // 좌우 약간의 여백
            ) {
                // forEach 대신 items 블록 사용
                items(favoriteAddresses) { address ->
                    FavoriteButton(
                        text = address.name,
                        onClick = { onFavoriteClick(address) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 직접 입력
            Button(
                onClick = onDirectInputClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("도착지 주소 직접 입력..", color = Color.Gray)
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
                        .background(Color.Green, RoundedCornerShape(50))
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
private fun FavoriteButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, color = Black)
    }
}