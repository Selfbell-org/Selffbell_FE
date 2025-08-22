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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkDetail
import com.selfbell.domain.model.SafeWalkStatus
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
        // 상단 헤더 (뒤로가기 버튼)
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
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = rememberCameraPositionState {
                            val start = LatLng(detail.origin.lat, detail.origin.lon)
                            position = CameraPosition.fromLatLngZoom(start, 15f)
                        }
                    ) {
                        Marker(
                            state = rememberMarkerState(position = LatLng(detail.origin.lat, detail.origin.lon)),
                            title = "출발",
                            snippet = detail.origin.addressText
                        )
                        Marker(
                            state = rememberMarkerState(position = LatLng(detail.destination.lat, detail.destination.lon)),
                            title = "도착",
                            snippet = detail.destination.addressText
                        )
                        Polyline(
                            points = listOf(
                                LatLng(detail.origin.lat, detail.origin.lon),
                                LatLng(detail.destination.lat, detail.destination.lon)
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            width = 8f
                        )
                    }

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
                    text = detail.ward.nickname,
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
                value = detail.expectedArrival?.let { expected -> // ✅ 'let'으로 안전하게 접근
                    "${detail.startedAt.format(DateTimeFormatter.ofPattern("HH:mm"))} ~ ${expected.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                } ?: "설정되지 않음" // ✅ null인 경우 처리
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem(
                label = "도착시간",
                value = if (detail.status == SafeWalkStatus.IN_PROGRESS) {
                    "진행 중"
                } else {
                    detail.endedAt?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "알 수 없음"
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem("주소", detail.destination.addressText)
        }
    }
}

// ✅ DetailItem 컴포저블은 수정할 필요 없습니다.
@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = Typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = Typography.bodyLarge)
    }
}