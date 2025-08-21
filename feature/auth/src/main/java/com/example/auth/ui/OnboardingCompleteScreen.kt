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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.theme.Typography
import com.example.auth.R
import kotlinx.coroutines.delay
import com.selfbell.auth.ui.AuthViewModel

@Composable
fun OnboardingCompleteScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // 프로필 이름 구독
    val profileName by viewModel.userName.collectAsState(initial = null)

    // 진입 시 프로필 호출 + 2초 후 이동
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        delay(2000)
        navController.navigate(AppRoute.HOME_ROUTE) {
            popUpTo(AppRoute.CONTACT_REGISTER_ROUTE) { inclusive = true }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_complete_icon),
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
            text = "${profileName ?: ""}님,\n환영합니다!",
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