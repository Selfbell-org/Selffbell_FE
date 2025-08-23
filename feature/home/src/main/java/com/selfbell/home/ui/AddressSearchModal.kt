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
import com.selfbell.domain.model.EmergencyBellDetail
import com.selfbell.domain.model.CriminalDetail
import android.util.Log // ✅ Log 클래스 임포트

// 모달의 상태를 정의하는 Enum
enum class ModalMode {
    SEARCH,
    DETAIL
}

@Composable
fun AddressSearchModal(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    mapMarkers: List<MapMarkerData>,
    onMarkerItemClick: (MapMarkerData) -> Unit,
    selectedEmergencyBellDetail: EmergencyBellDetail?,
    selectedCriminalDetail: CriminalDetail?,
    modalMode: ModalMode,
    onModalModeChange: (ModalMode) -> Unit = {},
    mapMarkerMode: MapMarkerMode, // ✅ mapMarkerMode 파라미터 추가
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
            Modifier.padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            when (modalMode) {
                ModalMode.SEARCH -> {
                    // ==== 검색 입력창 ====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = onSearchTextChange,
                            placeholder = { Text("내 주변 탐색", style = Typography.bodyMedium) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "검색",
                            modifier = Modifier.size(24.dp).clickable { onSearchClick() }
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    // ==== 마커 리스트 ====
                    val filteredMarkers = remember(mapMarkers, mapMarkerMode) {
                        Log.d("AddressSearchModal", "Recomposing marker list. Total items: ${mapMarkers.size}, Mode: $mapMarkerMode") // ✅ 로그 추가
                        if (mapMarkerMode == MapMarkerMode.SAFETY_BELL_ONLY) {
                            mapMarkers.filter { it.type == MapMarkerData.MarkerType.SAFETY_BELL }
                        } else {
                            mapMarkers // 모든 마커 표시
                        }
                    }

                    if (filteredMarkers.isEmpty() && searchText.isNotBlank()) {
                        Text(
                            text = "주변에 해당 정보가 없습니다.",
                            style = Typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                        )
                    } else if (filteredMarkers.isNotEmpty()){
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                        ) {
                            items(filteredMarkers) { markerData ->
                                Log.d("AddressSearchModal", "Displaying item: ${markerData.address}, Type: ${markerData.type}") // ✅ 로그 추가
                                Row(
                                    Modifier.fillMaxWidth().height(40.dp).clickable {
                                        if (markerData.type == MapMarkerData.MarkerType.SAFETY_BELL && markerData.objtId != null) {
                                            // 안심벨 마커 클릭 시 상세정보 모드로 전환
                                            onModalModeChange(ModalMode.DETAIL)
                                        } else if (markerData.type == MapMarkerData.MarkerType.CRIMINAL) {
                                            // 성범죄자 마커 클릭 시 상세정보 모드로 전환
                                            onModalModeChange(ModalMode.DETAIL)
                                        }
                                        onMarkerItemClick(markerData)
                                    },
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
                                        modifier = Modifier.weight(1f).padding(start = 14.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (markerData.distance >0) {
                                        Text("${markerData.distance.toInt()}m", style = Typography.bodyMedium)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
                ModalMode.DETAIL -> {
                    // ==== 상세 정보 (안심벨 또는 성범죄자) ====
                    when {
                        selectedEmergencyBellDetail != null -> {
                            // 안심벨 상세 정보
                            val detail = selectedEmergencyBellDetail
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.backstack_icon),
                                        contentDescription = "뒤로가기",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { onModalModeChange(ModalMode.SEARCH) }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "안심벨 상세 정보",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("상세 주소: ${detail.address}", style = Typography.bodyMedium)
                                Text("관리 전화: ${detail.managerTel}", style = Typography.bodyMedium)
                                Text("시설 종류: ${detail.type}", style = Typography.bodyMedium)
                                Text(
                                    text = "거리: ${detail.distance?.let { "${it.toInt()}m" } ?: "알 수 없음"}",
                                    style = Typography.bodyMedium
                                )
                                // TODO: 필요한 다른 정보 추가
                            }
                        }
                        selectedCriminalDetail != null -> {
                            // 성범죄자 상세 정보
                            val detail = selectedCriminalDetail
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.backstack_icon),
                                        contentDescription = "뒤로가기",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { onModalModeChange(ModalMode.SEARCH) }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "성범죄자 상세 정보",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                                                 Spacer(Modifier.height(16.dp))
                                 Text("이름: 성범죄자", style = Typography.bodyMedium)
                                 Text("유형: 거주지", style = Typography.bodyMedium)
                                 Text(
                                     text = "거리: ${detail.distanceMeters.toInt()}m",
                                     style = Typography.bodyMedium
                                 )
                            }
                        }
                        else -> {
                            Text("상세 정보를 불러오는 중...", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
            }
        }
    }
}