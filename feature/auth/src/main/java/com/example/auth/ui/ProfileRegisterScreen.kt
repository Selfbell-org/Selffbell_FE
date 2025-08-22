package com.example.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auth.R
import com.selfbell.core.navigation.AppRoute
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.core.ui.theme.Pretendard
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfbell.auth.ui.AuthUiState
import com.selfbell.auth.ui.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRegisterScreen(
    navController: NavController,
    phoneNumber: String,
    password: String,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var nickname by remember { mutableStateOf("") }
    val currentOnboardingStep = 1
    val totalOnboardingSteps = 4

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            navController.navigate(AppRoute.PERMISSION_ROUTE) {
                popUpTo(AppRoute.LANDING_ROUTE) { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            coroutineScope.launch {
                val errorMessage = (uiState as AuthUiState.Error).message
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Top Fixed Area (Onboarding Bar) ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OnboardingProgressBar(currentStep = currentOnboardingStep, totalSteps = totalOnboardingSteps)
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("SafeBell에서 사용할", style = MaterialTheme.typography.titleMedium, fontFamily = Pretendard)
                    Text("프로필 사진과 닉네임을 알려주세요.", style = MaterialTheme.typography.titleMedium, fontFamily = Pretendard)
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_icon2),
                            contentDescription = "프로필 사진",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
//                        Text(
//                            text = "",
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                fontFamily = Pretendard,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.White
//                            ),
//                            modifier = Modifier
//                                .align(Alignment.BottomCenter)
//                                .background(
//                                    color = Color(0x99000000),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .padding(vertical = 4.dp, horizontal = 12.dp)
//                                .clickable { /* TODO: 프로필 사진 변경 로직 구현 */ }
//                        )
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
                    text = if (uiState is AuthUiState.Loading) "등록 중..." else "확인",
                    onClick = {
//                        viewModel.bypassSignUp()
//일단 임시로 할게요
                        viewModel.signUp(
                            name = nickname,
                            phoneNumber = phoneNumber,
                            password = password
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nickname.isNotBlank() && uiState !is AuthUiState.Loading
                )
            }

            if (uiState is AuthUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
