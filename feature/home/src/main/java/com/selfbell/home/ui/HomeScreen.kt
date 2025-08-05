// feature/home/src/main/java/com/selfbell/feature/home/ui/HomeScreen.kt
package com.selfbell.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf // State를 위해 임포트
import androidx.compose.runtime.remember // State를 위해 임포트
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Typography
import com.selfbell.core.ui.composables.AppBarTwoLineTitle
import com.selfbell.core.ui.composables.AppBarCircleIcon

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    SelfBellTheme {
        Scaffold(
            topBar = {
                // 상단 헤더는 지도 위에 직접 배치되므로, Scaffold의 topBar 슬롯은 비워둡니다.
            },
            floatingActionButton = {
                // 112 신고 버튼은 Box의 하단에 직접 배치되므로, Scaffold의 floatingActionButton 슬롯은 비워둡니다.
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 1. 네이버 지도 API 영역 (가장 아래 레이어)
                    MapSection(modifier = Modifier.fillMaxSize())

                    // 2. 지도 위에 겹쳐지는 UI들을 Column으로 배치
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // 2.1. 상단 헤더 섹션 ("반갑습니다~" & 동행 버튼)
                        HomeTopBar(navController = navController)

                        // 2.2. 출발 버튼 섹션 (파라미터를 받도록 수정)
                        // TODO: 이 데이터는 ViewModel에서 가져올 것임
                        val homeAddressName = remember { mutableStateOf("우리집") }
                        val homeAddressDetail = remember { mutableStateOf("서울 특별시 서초구 반포본동...") }
                        DepartureSection(
                            navController = navController,
                            addressName = homeAddressName.value,
                            addressDetail = homeAddressDetail.value
                        )

                        // 2.3. 지도 관련 버튼 ('현위치')
                        MapControlButtons()

                        // 2.4. '내 주변 탐색' 섹션과 리스트
                        Spacer(modifier = Modifier.weight(1f)) // UI들을 상단에 배치하고 아래쪽 공백을 채움
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
private fun HomeTopBar(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "반갑습니다.", style = Typography.headlineMedium)
            Text(text = "오늘도 안전한 하루 되세요.", style = Typography.bodyMedium)
        }
        IconButton(onClick = { navController.navigate(AppRoute.ESCORT_ROUTE) }) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "동행 친구",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DepartureSection(
    navController: NavController,
    modifier: Modifier = Modifier,
    addressName: String, // <-- 데이터 파라미터 추가
    addressDetail: String // <-- 데이터 파라미터 추가
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 집 아이콘
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "집",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // 우리집 & 주소
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = addressName, style = Typography.titleMedium) // <-- 파라미터 사용
                Text(text = addressDetail, style = Typography.bodyMedium) // <-- 파라미터 사용
            }
            // 출발 버튼
            SelfBellButton(
                text = "출발",
                onClick = { /* TODO: 동선 공유 시작 화면으로 이동 */ },
                buttonType = SelfBellButtonType.PRIMARY_FILLED,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
private fun MapSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("네이버 지도 API 영역")
    }
}

@Composable
private fun MapControlButtons(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: 현위치 버튼 구현
    }
}


@Composable
private fun TodaysAlertSummarySection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(text = "내 주변 탐색")
        // TODO: 검색창 TextField 구현
        // TODO: 긴급신고/범죄자 리스트 구현
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelfBellTheme {
        HomeScreen(rememberNavController())
    }
}