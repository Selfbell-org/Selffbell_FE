package com.selfbell.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfbell.core.ui.theme.Typography
import com.selfbell.feature.home.R
import com.selfbell.home.model.MapMarkerData // MapMarkerData 임포트

@Composable
fun AddressSearchModal(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    mapMarkers: List<MapMarkerData>, // AlertItem 대신 MapMarkerData 사용
    onMarkerItemClick: (MapMarkerData) -> Unit // 콜백도 MapMarkerData 사용
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.93f)
            .wrapContentHeight()
            .shadow(
                elevation = 16.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xCCFFFFFF)) // 반투명 배경
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(24.dp)
            ),
        color = Color.Transparent // Surface 자체 색상은 투명하게
    ) {
        Column(
            Modifier
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // ==== 검색 입력창 ====
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { onSearchTextChange(it) },
                    placeholder = { Text("내 주변 탐색") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearchClick() }
                )
            }
            Spacer(Modifier.height(18.dp))

            // ==== 리스트 (MapMarkerData 기반) ====
            // 실제 앱에서는 이 리스트가 너무 길어지면 LazyColumn을 사용하는 것이 좋습니다.
            mapMarkers.forEach { markerData ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { onMarkerItemClick(markerData) }, // 클릭 시 markerData 전달
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = markerData.getIconResource()), // MapMarkerData에서 아이콘 가져오기
                        contentDescription = markerData.type.name, // 접근성을 위한 설명
                        modifier = Modifier.size(38.dp)
                    )
                    Text(
                        markerData.address, // 주소를 제목으로 사용
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp),
                        maxLines = 1, // 필요시 여러 줄 처리
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // 필요시 말줄임표
                    )
                    // MapMarkerData에 distance 필드가 있거나, 계산된 값을 사용
                    if (markerData.distance.isNotBlank()) {
                        Text(markerData.distance, style = Typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            if (mapMarkers.isEmpty() && searchText.isNotBlank()) {
                Text(
                    "주변에 해당 정보가 없습니다.",
                    style = Typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

