// auth/ui/OnboardingCompleteScreen.kt
package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.theme.Typography
import com.example.auth.R
import kotlinx.coroutines.delay

@Composable
fun OnboardingCompleteScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 2초 후 홈 화면으로 자동 이동
    LaunchedEffect(Unit) {
        delay(2000) // 2초 대기
        navController.navigate(AppRoute.HOME_ROUTE) {
            // 온보딩 관련 화면들을 백스택에서 모두 제거하여 뒤로가기 버튼으로 돌아올 수 없도록 함
            popUpTo(AppRoute.CONTACT_REGISTER_ROUTE) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_complete_icon), // 로그인 완료 아이콘 리소스 (예시)
            contentDescription = "로그인 완료",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "로그인 완료!",
            style = Typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "username님,\n환영합니다!",
            style = Typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingCompleteScreenPreview() {
    OnboardingCompleteScreen(navController = rememberNavController())
}