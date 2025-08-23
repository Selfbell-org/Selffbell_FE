package com.selfbell.home.ui

import androidx.compose.foundation.background
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
import com.naver.maps.map.overlay.Marker
import com.selfbell.core.ui.composables.ReportScreenHeader
import com.selfbell.core.ui.composables.ReusableNaverMap
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.SafeWalkDetail
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Primary
import java.time.Duration

@Composable
fun HistoryDetailScreen(
    sessionId: Long?,
    onBackClick: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val floatingBottomPadding = LocalFloatingBottomBarPadding.current

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
        modifier = modifier.shadow(
            elevation = 59.dp,
            spotColor = Color(0x40000000),
            ambientColor = Color(0x40000000))
            .fillMaxWidth()
            .height(244.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        // 👇 [수정] Column에 modifier와 적절한 arrangement를 다시 추가합니다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // 아이템 사이의 수직 간격
        ) {
            // --- 제목과 배지 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "위치 기록",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                text = detail.startedAt.format(dateFormatter) + " " + detail.startedAt.format(timeFormatter),
                style = Typography.bodyMedium,
                color = GrayInactive
            )

            // --- 설정 귀가 시간 ---
            val targetTime = detail.timerEnd ?: detail.expectedArrival
            if (targetTime != null) {
                val durationInMinutes = Duration.between(detail.startedAt, targetTime).toMinutes()
                val timeRangeText =
                    "${detail.startedAt.format(timeFormatter)} ~ ${targetTime.format(timeFormatter)} (${durationInMinutes}분)"
                DetailItem(
                    label = "설정 귀가 시간",
                    value = timeRangeText
                )
            }

            // --- 도착시간 ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                val arrivalTimeText = detail.endedAt?.format(timeFormatter) ?: "진행 중"
                DetailItem(
                    label = "도착 시간",
                    value = arrivalTimeText
                )

                if (detail.endedAt != null && targetTime != null) {
                    val differenceInMinutes = Duration.between(detail.endedAt, targetTime).toMinutes()
                    val differenceText = when {
                        differenceInMinutes > 0 -> "(예상보다 ${differenceInMinutes}분 일찍 도착)"
                        differenceInMinutes < 0 -> "(예상보다 ${-differenceInMinutes}분 늦게 도착)"
                        else -> "(예상 시간과 동일)"
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = differenceText,
                        style = Typography.bodyMedium,
                        color = GrayInactive
                    )
                }
            }

            // --- 주소 ---
            DetailItem(
                label = "주소",
                value = detail.destination.addressText
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$label ",
            style = Typography.bodyMedium,
            color = GrayInactive
        )
        Text(
            text = value,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}