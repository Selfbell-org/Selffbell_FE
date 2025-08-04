// feature/home/src/main/java/com/selfbell/feature/home/ui/HomeScreen.kt
package com.selfbell.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn // LazyColumn 임포트
import androidx.compose.material3.FabPosition // FabPosition 임포트
import androidx.compose.material3.Scaffold // Scaffold 임포트
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // 미리보기용 rememberNavController
import com.selfbell.core.ui.composables.SelfBellButton // SelfBellButton 임포트
import com.selfbell.core.ui.composables.SelfBellButtonType // SelfBellButtonType 임포트
import com.selfbell.core.ui.theme.SelfBellTheme // 테마 임포트


@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { /* TODO: 상단 헤더 */ }, // 상단 헤더 섹션 구현 예정
        floatingActionButton = {
            // 112 신고 FAB (Floating Action Button)
            SelfBellButton(
                text = "112 신고",
                onClick = { /* TODO: 112 신고 화면으로 이동 */ navController.navigate("emergency_call_route") }, // AppRoute.EMERGENCY_CALL_ROUTE
                buttonType = SelfBellButtonType.FAB // FAB 타입 버튼
            )
        },
        floatingActionButtonPosition = FabPosition.End, // 우측 하단에 고정
        content = { paddingValues -> // content 람다를 통해 Scaffold의 패딩을 받음
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Scaffold의 기본 패딩 적용
                    .padding(horizontal = 16.dp) // 화면 좌우 공통 패딩 (조절 가능)
            ) {
                // 1. 맨 위 헤더 섹션
                item { GreetingSection() }

                // 2. 네이버 지도 API 섹션 (나중에 구현)
                item { NaverMapPlaceholderSection() }

                // 3. 오늘 날짜 및 신규 등록 요약 섹션
                item { TodaysAlertSummarySection() }

                // 4. 범죄자 리스트 목록 (나중에 구현)
                item { CrimeListSection() }

                // 5. 동선 공유 헤더
                item { EscortShareHeader() }

                // 6. 친구 리스트 목록 카드 (임시로 5개)
                items(5) { // 임시로 5개의 친구 카드 표시
                    EscortFriendCard()
                }

                // 하단 FAB와 바텀바를 위한 여백 (스크롤 시 FAB가 가려지지 않도록)
                item { Spacer(modifier = Modifier.height(96.dp)) } // 바텀바 높이 + FAB 여백 고려 (조절 가능)
            }
        }
    )
}


// -------------------------------------------------------------
// 하위 섹션 Composable 함수들 (아직은 빈 형태로 정의)
// -------------------------------------------------------------

@Composable
fun GreetingSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(text = "반갑습니다~", style = Typography.headlineMedium) // Figma Header 스타일
        Text(text = "좋은 하루 되세요", style = Typography.bodyMedium) // Figma Body 스타일
        // TODO: 알림 버튼 (종 모양) 추가
    }
}

@Composable
fun NaverMapPlaceholderSection(modifier: Modifier = Modifier) {
    // 임시 지도 자리 표시자
    Text("네이버 지도 API 영역", modifier = modifier.fillMaxWidth().height(200.dp))
}

@Composable
fun TodaysAlertSummarySection(modifier: Modifier = Modifier) {
    // 오늘 날짜 및 신규 등록 요약 자리 표시자
    Text("오늘 날짜 및 신규 등록 요약", modifier = modifier.fillMaxWidth().padding(vertical = 8.dp))
}

@Composable
fun CrimeListSection(modifier: Modifier = Modifier) {
    // 범죄자 리스트 목록 자리 표시자
    Text("범죄자 리스트 목록", modifier = modifier.fillMaxWidth().height(300.dp).padding(vertical = 8.dp))
}

@Composable
fun EscortShareHeader(modifier: Modifier = Modifier) {
    // 동선 공유 헤더 자리 표시자
    Text("동선 공유 - 모두보기", modifier = modifier.fillMaxWidth().padding(vertical = 8.dp))
}

@Composable
fun EscortFriendCard(modifier: Modifier = Modifier) {
    // 친구 리스트 목록 카드 자리 표시자
    Text("친구 카드", modifier = modifier.fillMaxWidth().height(100.dp).padding(vertical = 4.dp))
}

// 미리보기 Composable
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelfBellTheme {
        // 미리보기에서는 NavController가 필요 없지만, 컴포저블 인자 맞추기 위해 더미 전달
        HomeScreen(rememberNavController())
    }
}