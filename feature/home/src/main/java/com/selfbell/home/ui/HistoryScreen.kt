package com.selfbell.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.HistoryUserFilter
import com.selfbell.home.ui.composables.HistoryCardItem
import com.selfbell.domain.model.SafeWalkHistoryItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.sp
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Primary

@Composable
fun HistoryScreen(
    onNavigateToDetail: (sessionId: Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val floatingBottomPadding = LocalFloatingBottomBarPadding.current


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ [추가] 화면 최상단에 "히스토리" 타이틀 추가
        Text(
            text = "히스토리",
            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        )

        HistoryFilterButtons(
            selectedFilter = currentFilter.userType,
            onFilterSelected = { newFilterType ->
                viewModel.setFilter(
                    currentFilter.copy(userType = newFilterType)
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryDateFilterDropdown(
                selectedFilter = currentFilter.dateRange,
                onFilterSelected = { newDateFilter ->
                    viewModel.setFilter(
                        currentFilter.copy(dateRange = newDateFilter)
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            HistorySortDropdown(
                selectedSortOrder = currentFilter.sortOrder,
                onSortSelected = { newSortOrder ->
                    viewModel.setFilter(
                        currentFilter.copy(sortOrder = newSortOrder)
                    )
                }
            )
        }

        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HistoryUiState.Success -> {
                if (state.historyItems.isEmpty()) {
                    EmptyHistoryScreen()
                } else {
                    HistoryList(
                        historyItems = state.historyItems,
                        onNavigateToDetail = onNavigateToDetail,
                        contentPadding = floatingBottomPadding
                    )
                }
            }
        }
    }
}

// ✅ [수정] HistoryFilterButtons Composable 전체 수정
@Composable
private fun HistoryFilterButtons(
    selectedFilter: HistoryUserFilter,
    onFilterSelected: (HistoryUserFilter) -> Unit
) {
    // 버튼들을 감싸는 둥근 배경
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = GrayInactive.copy(alpha = 0.1f) // 배경색
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp), // 내부 패딩
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HistoryUserFilter.values().forEach { filterType ->
                val isSelected = selectedFilter == filterType

                // 개별 버튼
                TextButton(
                    onClick = { onFilterSelected(filterType) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isSelected) Primary else Color.Transparent,
                        contentColor = if (isSelected) Color.White else GrayInactive
                    )
                ) {
                    Text(
                        text = when (filterType) {
                            HistoryUserFilter.GUARDIANS -> "보호자/피보호자"
                            HistoryUserFilter.MINE -> "나의 귀가"
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    historyItems: List<SafeWalkHistoryItem>,
    onNavigateToDetail: (sessionId: Long) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = contentPadding
    ) {
        items(historyItems, key = { it.sessionId }) { item ->
            HistoryCardItem(
                historyItem = item,
                onClick = { onNavigateToDetail(item.sessionId) }
            )
            Divider()
        }
    }
}

@Composable
fun EmptyHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "요청 내역이 없습니다",
            style = Typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}