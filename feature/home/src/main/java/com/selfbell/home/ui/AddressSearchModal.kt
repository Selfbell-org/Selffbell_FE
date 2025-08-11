// home/ui/AddressSearchModal.kt
package com.selfbell.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.selfbell.home.model.MapMarkerData
import androidx.compose.ui.text.style.TextOverflow
import com.selfbell.core.ui.theme.SelfBellTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddressSearchModal(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    mapMarkers: List<MapMarkerData>,
    onMarkerItemClick: (MapMarkerData) -> Unit
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
            .background(Color(0xCCFFFFFF))
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(24.dp)
            ),
        color = Color.Transparent
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
                    placeholder = { Text("내 주변 탐색", style = Typography.bodyMedium) },
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

            // ==== 리스트 (LazyColumn으로 변경) ====
            if (mapMarkers.isEmpty() && searchText.isNotBlank()) {
                Text(
                    text = "주변에 해당 정보가 없습니다.",
                    style = Typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else if (mapMarkers.isNotEmpty()){
                // LazyColumn을 사용하여 스크롤 가능하게 만듭니다.
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                ) {
                    items(mapMarkers) { markerData ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable { onMarkerItemClick(markerData) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = markerData.getIconResource()),
                                contentDescription = markerData.type.name,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                markerData.address,
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (markerData.distance.isNotBlank()) {
                                Text(markerData.distance, style = Typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}