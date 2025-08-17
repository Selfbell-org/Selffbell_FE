package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Pretendard // Pretendard 폰트 임포트
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileRegisterScreen(navController: NavController, modifier: Modifier = Modifier) {
    var nickname by remember { mutableStateOf("") }
    val currentOnboardingStep = 1 // 프로필 등록은 3단계
    val totalOnboardingSteps = 4

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
            OnboardingProgressBar(currentStep = currentOnboardingStep, totalSteps = totalOnboardingSteps)
            Spacer(modifier = Modifier.height(20.dp))

            Text("SafeBell에서 사용할", style = MaterialTheme.typography.titleMedium, fontFamily = Pretendard)
            Text("프로필 사진과 닉네임을 알려주세요.", style = MaterialTheme.typography.titleMedium, fontFamily = Pretendard)
            Spacer(modifier = Modifier.height(32.dp))

            // 수정된 프로필 이미지 컴포넌트
            Box(
                modifier = Modifier.size(140.dp), // 크기를 140dp로 변경
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_profile_icon2),
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop // 이미지 비율을 유지하며 원형에 맞게 자르기
                )
                // '수정' 문구를 이미지 위에 배치
                Text(
                    text = "수정",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = Pretendard, // Pretendard 폰트 사용
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(
                            color = Color(0x99000000), // 반투명 검정색으로 변경
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 12.dp)
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