package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.selfbell.core.ui.composables.OnboardingProgressBar // OnboardingProgressBar 임포트
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileRegisterScreen(navController: NavController, modifier: Modifier = Modifier) {
    var nickname by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 온보딩 프로그레스바 배치
            Spacer(modifier = Modifier.height(20.dp))
            OnboardingProgressBar(currentStep = 1, totalSteps = 4) // 총 5단계 중 1단계
            Spacer(modifier = Modifier.height(20.dp))

            Text("프로필을 등록해주세요", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_profile_icon2), // 더미 프로필 아이콘
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape) // 원형으로 자르기
                )
                // '수정' 문구를 이미지 위에 배치
                Text(
                    text = "수정",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(com.selfbell.core.R.font.pretendard_medium)),
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(
                            color = Color(0x99FFFFFF), // Figma의 반투명 흰색 배경
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp) // 위쪽만 둥글게
                        )
                        .padding(vertical = 4.dp, horizontal = 12.dp) // <-- 텍스트 위아래, 좌우 패딩 추가
                        .clickable {
                            // TODO: 프로필 사진 변경 로직 구현
                        }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SelfBellButton(
            text = "확인",
            onClick = {
                // 권한 페이지로 이동
                navController.navigate(AppRoute.PERMISSION_ROUTE)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileRegisterScreenPreview() {
    SelfBellTheme {
        ProfileRegisterScreen(navController = rememberNavController())
    }
}
