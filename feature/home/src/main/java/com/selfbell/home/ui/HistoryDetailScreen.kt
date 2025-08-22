package com.selfbell.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
//import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.ReusableNaverMap // ✅ ReusableNaverMap import
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkDetail
import java.time.format.DateTimeFormatter

@Composable
fun HistoryDetailScreen(
    sessionId: Long?,
    onBackClick: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.loadSafeWalkDetail(sessionId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ReportScreenHeader(
            title = "히스토리 - 상세 내역",
            showCloseButton = false,
            onCloseClick = {},
            showBackButton = true,
            onBackClick = onBackClick
        )

        when (val state = uiState) {
            is HistoryDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HistoryDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HistoryDetailUiState.Success -> {
                val detail = state.detail
                val startLatLng = LatLng(detail.origin.lat, detail.origin.lon)
                val endLatLng = LatLng(detail.destination.lat, detail.destination.lon)

                Box(modifier = Modifier.fillMaxSize()) {
                    // ✅ ReusableNaverMap 사용
                    ReusableNaverMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPosition = startLatLng,
                        onMapReady = { naverMap ->
                            // 시작점 마커
                            Marker().apply {
                                position = startLatLng
                                captionText = "출발"
                                map = naverMap
                            }
                            // 도착점 마커
                            Marker().apply {
                                position = endLatLng
                                captionText = "도착"
                                map = naverMap
                            }
                            // 경로
                            PathOverlay().apply {
                                coords = listOf(startLatLng, endLatLng)
                                color = Color.Blue.hashCode()
                                width = 10
                                map = naverMap
                            }
                        }
                    )

                    HistoryDetailCard(
                        detail = detail,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ... HistoryDetailCard와 DetailItem Composable은 이전과 동일하게 유지 ...
@Composable
fun HistoryDetailCard(
    detail: SafeWalkDetail,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("위치 기록", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = detail.ward.name, // nickname -> name으로 변경
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detail.startedAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 오전 HH시 mm분")),
                style = Typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            DetailItem(
                label = "상대가 설정한 시간",
                value = detail.expectedArrival?.let { expected ->
                    "${detail.startedAt.format(DateTimeFormatter.ofPattern("HH:mm"))} ~ ${expected.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                } ?: "설정되지 않음"
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem(
                label = "도착시간",
                value = when (detail.status) { // SafeWalkStatus enum 대신 String 사용
                    "IN_PROGRESS" -> "진행 중"
                    "COMPLETED" -> "완료됨"
                    "ENDED" -> "종료됨"
                    "CANCELED" -> "취소됨"
                    else -> "알 수 없음"
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem("주소", detail.destination.addressText)
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = Typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = Typography.bodyLarge)
    }
}