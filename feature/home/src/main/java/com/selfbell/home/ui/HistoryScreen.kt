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

@Composable
fun HistoryScreen(
    onNavigateToDetail: (sessionId: Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ 3가지 탭
        TabRow(selectedTabIndex = viewModel.currentFilter.value.userType.ordinal) {
            HistoryUserFilter.values().forEach { filterType ->
                Tab(
                    selected = viewModel.currentFilter.value.userType == filterType,
                    onClick = {
                        viewModel.setFilter(
                            viewModel.currentFilter.value.copy(userType = filterType)
                        )
                    },
                    text = {
                        Text(text = when (filterType) {
                            HistoryUserFilter.ALL -> "전체 기록"
                            HistoryUserFilter.GUARDIANS -> "보호자/피보호자"
                            HistoryUserFilter.MINE -> "나의 귀가"
                        })
                    }
                )
            }
        }

        // ✅ 필터 드롭다운
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryDateFilterDropdown(
                selectedFilter = viewModel.currentFilter.value.dateRange,
                onFilterSelected = { newDateFilter ->
                    viewModel.setFilter(
                        viewModel.currentFilter.value.copy(dateRange = newDateFilter)
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            HistorySortDropdown(
                selectedSortOrder = viewModel.currentFilter.value.sortOrder,
                onSortSelected = { newSortOrder ->
                    viewModel.setFilter(
                        viewModel.currentFilter.value.copy(sortOrder = newSortOrder)
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
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
            }
        }
    }
}

// ✅ 히스토리 목록을 보여주는 Composable
@Composable
fun HistoryList(
    historyItems: List<SafeWalkHistoryItem>,
    onNavigateToDetail: (sessionId: Long) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(historyItems, key = { it.sessionId }) { item -> // id -> sessionId로 변경
            HistoryCardItem(
                historyItem = item,
                onClick = { onNavigateToDetail(item.sessionId) } // id -> sessionId로 변경
            )
            Divider()
        }
    }
}

// ✅ 내역이 없을 때 화면
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