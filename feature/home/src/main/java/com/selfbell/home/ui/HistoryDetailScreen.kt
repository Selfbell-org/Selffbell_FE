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
import com.selfbell.domain.model.SafeWalkStatus
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Primary

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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 제목과 배지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "위치 기록",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // 배지
                Surface(
                    color = Primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = detail.ward.nickname,
                        style = Typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 날짜와 시간
            Text(
                text = detail.startedAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 오전 HH시 mm분")),
                style = Typography.bodyMedium,
                color = GrayInactive
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 상대가 설정한 시간
            val expectedStartTime = detail.expectedStartTime
            val expectedEndTime = detail.expectedEndTime
            val estimatedDuration = detail.estimatedDurationMinutes
            
            if (expectedStartTime != null && expectedEndTime != null) {
                Row {
                    Text(
                        text = "상대가 설정한 시간 ",
                        style = Typography.bodyMedium,
                        color = GrayInactive
                    )
                    Text(
                        text = "${expectedStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))} ~ ${expectedEndTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = " (${estimatedDuration ?: java.time.Duration.between(expectedStartTime, expectedEndTime).toMinutes()}분)",
                        style = Typography.bodyMedium,
                        color = GrayInactive
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 도착시간
            val arrivalTime = if (detail.status == SafeWalkStatus.IN_PROGRESS) {
                "진행 중"
            } else {
                detail.endedAt?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "알 수 없음"
            }
            
            val endedAt = detail.endedAt
            val timeDifference = detail.timeDifferenceMinutes
            
            Row {
                Text(
                    text = "도착시간 ",
                    style = Typography.bodyMedium,
                    color = GrayInactive
                )
                Text(
                    text = arrivalTime,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (timeDifference != null && endedAt != null) {
                    val timeText = when {
                        timeDifference > 0 -> " (예상 도착 시간 ${timeDifference}분 후)"
                        timeDifference < 0 -> " (예상 도착 시간 ${-timeDifference}분 전)"
                        else -> " (예상 도착 시간과 동일)"
                    }
                    Text(
                        text = timeText,
                        style = Typography.bodyMedium,
                        color = GrayInactive
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 주소
            Row {
                Text(
                    text = "주소 ",
                    style = Typography.bodyMedium,
                    color = GrayInactive
                )
                Text(
                    text = detail.destination.addressText,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}