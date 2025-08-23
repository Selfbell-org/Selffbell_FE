package com.selfbell.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfbell.core.ui.theme.Typography
import com.selfbell.domain.model.HistoryUserFilter
import com.selfbell.home.ui.composables.HistoryCardItem
import com.selfbell.home.ui.HistoryDateFilterDropdown
import com.selfbell.home.ui.HistorySortDropdown
import com.selfbell.domain.model.SafeWalkHistoryItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.insets.LocalFloatingBottomBarPadding
import com.selfbell.core.R as CoreR

@Composable
fun HistoryScreen(
    onNavigateToDetail: (sessionId: Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ 수정: collectAsState()를 사용하여 currentFilter 상태를 구독
    val currentFilter by viewModel.currentFilter.collectAsState()
    val floatingBottomPadding = LocalFloatingBottomBarPadding.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ 3가지 탭을 대신할 버튼 그룹 Composable
        HistoryFilterButtons(
            selectedFilter = currentFilter.userType, // ✅ 수정: .value 제거
            onFilterSelected = { newFilterType ->
                viewModel.setFilter(
                    currentFilter.copy(userType = newFilterType) // ✅ 수정: .value 제거
                )
            }
        )

        // ✅ 필터 드롭다운
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryDateFilterDropdown(
                selectedFilter = currentFilter.dateRange, // ✅ 수정: .value 제거
                onFilterSelected = { newDateFilter ->
                    viewModel.setFilter(
                        currentFilter.copy(dateRange = newDateFilter) // ✅ 수정: .value 제거
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            HistorySortDropdown(
                selectedSortOrder = currentFilter.sortOrder, // ✅ 수정: .value 제거
                onSortSelected = { newSortOrder ->
                    viewModel.setFilter(
                        currentFilter.copy(sortOrder = newSortOrder) // ✅ 수정: .value 제거
                    )
                }
            )
        }

        // ✅ UI 상태에 따른 화면 분기
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
@Composable
private fun HistoryFilterButtons(
    selectedFilter: HistoryUserFilter,
    onFilterSelected: (HistoryUserFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HistoryUserFilter.values().forEach { filterType ->
            val isSelected = selectedFilter == filterType

            val buttonType = if (isSelected) {
                SelfBellButtonType.PRIMARY_FILLED
            } else {
                SelfBellButtonType.OUTLINED
            }

            // ✅ SelfBellButton 컴포넌트 사용
            SelfBellButton(
                text = when (filterType) {
                    //HistoryUserFilter.ALL -> "전체 기록"
                    HistoryUserFilter.GUARDIANS -> "보호자/피보호자"
                    HistoryUserFilter.MINE -> "나의 귀가"
                },
                onClick = { onFilterSelected(filterType) },
                // ✅ weight(1f) 제거. 텍스트 길이에 맞춰 너비가 유동적으로 변함
                modifier = Modifier,
                buttonType = buttonType,
                isSmall = true,
            )
        }
    }
}
@Composable
fun HistoryList(
    historyItems: List<SafeWalkHistoryItem>,
    onNavigateToDetail: (sessionId: Long) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = contentPadding) {
        items(historyItems, key = { it.sessionId }) { item -> // id -> sessionId로 변경
            HistoryCardItem(
                historyItem = item,
                onClick = { onNavigateToDetail(item.sessionId) } // id -> sessionId로 변경
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