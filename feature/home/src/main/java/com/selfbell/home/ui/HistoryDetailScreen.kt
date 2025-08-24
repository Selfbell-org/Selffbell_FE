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
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkDetail
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.PolylineOverlay
import com.selfbell.core.ui.theme.Black
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Primary
import java.time.Duration
import java.time.ZonedDateTime

@Composable
fun HistoryDetailScreen(
    sessionId: Long?,
    onBackClick: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val floatingBottomPadding = LocalFloatingBottomBarPadding.current
    val trackCoordinates by viewModel.trackCoordinates.collectAsState()


    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.loadSafeWalkDetail(sessionId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    ReusableNaverMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPosition = startLatLng,
                        onMapReady = { naverMap ->
                            Marker().apply {
                                position = startLatLng
                                captionText = "출발"
                                map = naverMap
                            }
                            Marker().apply {
                                position = endLatLng
                                captionText = "도착"
                                map = naverMap
                            }
                            if (trackCoordinates.size >= 2) {
                                PolylineOverlay().apply {
                                    coords = trackCoordinates
                                    color = Color(0xFF007AFF).hashCode()
                                    width = 12
                                    capType = PolylineOverlay.LineCap.Round
                                    joinType = PolylineOverlay.LineJoin.Round
                                    map = naverMap
                                }

                                val bounds = LatLngBounds.from(trackCoordinates)
                                val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
                                naverMap.moveCamera(cameraUpdate)
                            }
                        }
                    )

                    HistoryDetailCard(
                        detail = detail,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(floatingBottomPadding)
                            .padding(bottom = 24.dp)
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
    // 날짜/시간 포맷터 정의
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Surface(
        modifier = modifier.fillMaxWidth()
            .height(244.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // 모든 아이템의 수직 간격을 16dp로 통일
        ) {
            // --- 제목과 배지 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "위치 기록",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = detail.ward.nickname,
                        style = Typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // --- 날짜와 시간 ---
            Text(
                text = detail.startedAt.plusHours(9).format(dateFormatter) + " " + detail.startedAt.plusHours(9).format(timeFormatter),
                style = Typography.bodyMedium,
                color = GrayInactive
            )

            // --- 상세 정보 섹션 ---
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp) // 상세 정보 항목들 간의 간격
            ) {
                val targetTime = detail.timerEnd ?: detail.expectedArrival

                // --- 설정 귀가 시간 ---
                if (targetTime != null) {
                    DetailRow(
                        label = "상대가 설정한 시간"
                    ) {
                        val durationInMinutes = Duration.between(detail.startedAt, targetTime).toMinutes()
                        Text(
                            text = "${detail.startedAt.plusHours(9).format(timeFormatter)} ~ ${targetTime.plusHours(9).format(timeFormatter)}",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = " (${durationInMinutes}분)",
                            style = Typography.bodyLarge,
                            color = GrayInactive
                        )
                    }
                }

                // --- 도착시간 ---
                DetailRow(
                    label = "도착시간"
                ) {
                    val arrivalTimeText = detail.endedAt?.plusHours(9)?.format(timeFormatter) ?: "진행 중"
                    Text(
                        text = arrivalTimeText,
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    if (detail.endedAt != null && targetTime != null) {
                        val differenceInMinutes = Duration.between(detail.endedAt, targetTime).toMinutes()
                        val differenceText = when {
                            differenceInMinutes > 0 -> "(${differenceInMinutes}분 일찍 도착)"
                            differenceInMinutes < 0 -> "(${-differenceInMinutes}분 늦게 도착)"
                            else -> "(예상 시간과 동일)"
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = differenceText,
                            style = Typography.bodyLarge, // 👈 [수정] Typography.bodyLarge로 통일
                            color = GrayInactive
                        )
                    }
                }

                // --- 주소 ---
                DetailRow(
                    label = "주소"
                ) {
                    Text(
                        text = detail.destination.addressText,
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ✅ [추가] 정렬을 위한 새로운 DetailRow 컴포저블
@Composable
private fun DetailRow(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 라벨에 고정된 너비를 주어 모든 값들이 수직으로 정렬되도록 함
        Text(
            text = label,
            style = Typography.bodyLarge, // 👈 [수정] Typography.bodyLarge로 통일
            color = GrayInactive,
            modifier = Modifier.width(120.dp) // 👈 라벨 너비 고정
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}