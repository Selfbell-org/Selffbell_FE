package com.selfbell.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.selfbell.domain.model.HistoryDateFilter
import com.selfbell.domain.model.HistorySortOrder
import com.selfbell.core.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDateFilterDropdown(
    selectedFilter: HistoryDateFilter,
    onFilterSelected: (HistoryDateFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = when (selectedFilter) {
                        HistoryDateFilter.WEEK -> "최근 1주일"
                        HistoryDateFilter.MONTH -> "최근 30일"
                        HistoryDateFilter.YEAR -> "최근 1년"
                        HistoryDateFilter.ALL -> "전체"
                    },
                    style = Typography.labelMedium
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HistoryDateFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (filter) {
                                HistoryDateFilter.WEEK -> "최근 1주일"
                                HistoryDateFilter.MONTH -> "최근 30일"
                                HistoryDateFilter.YEAR -> "최근 1년"
                                HistoryDateFilter.ALL -> "전체"
                            },
                            style = Typography.bodyMedium
                        )
                    },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySortDropdown(
    selectedSortOrder: HistorySortOrder,
    onSortSelected: (HistorySortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = when (selectedSortOrder) {
                        HistorySortOrder.LATEST -> "최신순"
                        HistorySortOrder.OLDEST -> "오래된순"
                    },
                    style = Typography.labelMedium
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HistorySortOrder.values().forEach { sortOrder ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (sortOrder) {
                                HistorySortOrder.LATEST -> "최신순"
                                HistorySortOrder.OLDEST -> "오래된순"
                            },
                            style = Typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSortSelected(sortOrder)
                        expanded = false
                    }
                )
            }
        }
    }
}