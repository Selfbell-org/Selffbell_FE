package com.selfbell.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.selfbell.core.R
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.composables.AppBarTwoLineTitle
import com.selfbell.core.ui.composables.AppBarCircleIcon
import com.selfbell.core.ui.composables.SelfBellAppBar
import com.selfbell.core.ui.theme.GrayInactive as Gray100
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import com.selfbell.core.ui.composables.SelfBellBottomNavigation

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    SelfBellTheme {
        Scaffold(
            topBar = {
                SelfBellAppBar(
                    titleContent = {
                        AppBarTwoLineTitle(
                            title = "반갑습니다.",
                            subtitle = "오늘도 안전한 하루 되세요."
                        )
                    },
                    actions = {
                        AppBarCircleIcon(
                            iconResId = R.drawable.ic_fellow,
                            contentDescription = "동행 친구",
                            onClick = { navController.navigate(AppRoute.ESCORT_ROUTE) }
                        )
                    }
                )
            },
            bottomBar = {
                // Scaffold의 bottomBar 슬롯에 SelfBellBottomNavigation을 연결합니다.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .clip(RoundedCornerShape(40.dp)),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        SelfBellBottomNavigation(navController = navController)
                    }
                }
            },
            containerColor = Color.Transparent, // <- Scaffold의 배경색을 투명하게 설정
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // 1. 네이버 지도 API 영역 (가장 아래 레이어)
                    MapSection(modifier = Modifier.fillMaxSize())

                    // 2. 지도 위에 겹쳐지는 UI들을 Column으로 배치
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues) // Scaffold의 padding을 Column에 적용
                            .padding(horizontal = 16.dp)
                    ) {
                        // DepartureSection 등의 상위 UI는 TopBar에 의해 밀려나게 됩니다.
                        // 이전에 Box에 직접 배치했던 UI들을 여기에 넣습니다.
                        DepartureSection(
                            navController = navController,
                            addressName = remember { mutableStateOf("우리집") }.value,
                            addressDetail = remember { mutableStateOf("서울 특별시 서초구 반포본동...") }.value
                        )

                        MapControlButtons()

                        // Spacer를 사용하여 남은 공간을 모두 차지하게 만들어,
                        // TodaysAlertSummarySection을 바텀바 위로 밀어냅니다.
                        Spacer(modifier = Modifier.weight(1f))

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
private fun DepartureSection(
    navController: NavController,
    modifier: Modifier = Modifier,
    addressName: String,
    addressDetail: String
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp)
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "집",
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = addressName, style = Typography.titleMedium)
                Text(text = addressDetail, style = Typography.bodyMedium)
            }
            SelfBellButton(
                text = "출발",
                onClick = { /* TODO: 동선 공유 시작 화면으로 이동 */ },
                buttonType = SelfBellButtonType.PRIMARY_FILLED,
                isSmall = true,
                modifier = Modifier.wrapContentWidth()
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
        Image(
            painter = painterResource(id = R.drawable.map_placeholder),
            contentDescription = "지도 배경",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MapControlButtons(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: '현위치' 버튼 구현
    }
}


@Composable
fun TodaysAlertSummarySection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x80FFFFFF))
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(24.dp)
            ),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // '내주변탐색' 검색 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "내 주변 탐색",
                    color = Color.Black,
                    style = Typography.bodyMedium
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 긴급신고-선유공원앞 SOS 레이아웃
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sos_map),
                    contentDescription = "SOS 아이콘",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "긴급신고-선유공원앞",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
                Text(
                    text = "358m",
                    style = Typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 범죄자 위치정보 레이아웃 (SOS와 동일한 구조)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_crime_map),
                    contentDescription = "경고 아이콘",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "범죄자 위치정보",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
                Text(
                    text = "421m",
                    style = Typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelfBellTheme {
        HomeScreen(rememberNavController())
    }
}
