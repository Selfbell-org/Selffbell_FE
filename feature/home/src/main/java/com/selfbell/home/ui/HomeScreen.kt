// feature/home/ui/HomeScreen.kt
package com.selfbell.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Typography
import androidx.compose.ui.Alignment // Alignment 임포트
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    SelfBellTheme {
        Scaffold(
            topBar = { /* TODO: 필요시 상단 AppBar */ },
            // floatingActionButton 슬롯 제거
            content = { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. 네이버 지도 API 영역 (가장 아래 레이어, 전체 화면 차지)
                    MapSection(modifier = Modifier.fillMaxSize())

                    // 2. 지도 위에 겹쳐지는 UI들을 Column으로 배치
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues) // Scaffold의 패딩 적용
                    ) {
                        // 2.1. 상단 헤더 섹션 (2줄 문구 + 동행 버튼)
                        GreetingSection()

                        // 2.2. 출발 버튼 섹션
                        DepartureSection()

                        // 2.3. 지도 관련 버튼들 ('현위치', 'SOS')
                        MapControlButtons()

                        // 2.4. '내 주변 탐색' 섹션 (LazyColumn으로 구현)
                        TodaysAlertSummarySection()
                    }
                }
            }
        )
    }
}
// -------------------------------------------------------------
// 하위 섹션 Composable 함수들
// -------------------------------------------------------------

@Composable
fun GreetingSection(modifier: Modifier = Modifier) { /* ... 구현 예정 ... */ }

@Composable
fun DepartureSection(modifier: Modifier = Modifier) { /* ... 구현 예정 ... */ }

@Composable
fun MapSection(modifier: Modifier = Modifier) {
    // 네이버 지도 API 영역을 임시로 표시
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("네이버 지도 API 영역")
    }
}

@Composable
fun MapControlButtons(modifier: Modifier = Modifier) {
    // '현위치' 버튼 등 지도 위에 겹쳐지는 버튼들
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // 버튼 간 간격
    ) {
        // TODO: 현위치 버튼 구현
        // TODO: SOS 버튼 구현
    }
}


@Composable
fun TodaysAlertSummarySection(modifier: Modifier = Modifier) {
    // '내 주변 탐색' 검색창과 리스트
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "내 주변 탐색")
        // TODO: 검색창 TextField 구현
        // TODO: 긴급신고/범죄자 리스트 구현
    }
}

// ... 다른 섹션 Composable은 필요 없어짐 ...

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelfBellTheme {
        HomeScreen(rememberNavController())
    }
}