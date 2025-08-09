package com.example.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.R
import com.selfbell.core.ui.theme.Pretendard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onRegister: () -> Unit,
    onNavigateUp: () -> Unit = {}
) {
    Scaffold( // Scaffold로 전체 화면 구조를 감쌈
        topBar = {
            TopAppBar(
                title = { /* 제목이 필요하다면 여기에 Text("회원가입") 또는 "프로필 등록" 등 추가 */ },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(R.drawable.backstack_icon), // Material 기본 뒤로가기 아이콘
                            contentDescription = "뒤로가기"
                        )
                        // 만약 커스텀 아이콘을 사용한다면:
                        // Icon(
                        // painter = painterResource(id = R.drawable.your_back_icon),
                        // contentDescription = "뒤로가기"
                        // )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White // TopAppBar 배경색을 흰색으로 설정
                )
            )
        },
        containerColor = Color.White // Scaffold 전체 배경색
    ) { paddingValues -> // Scaffold로부터 content padding을 받음
        Column(
            modifier = Modifier
                .fillMaxSize()
                // .background(Color.White) // 배경색은 Scaffold로 이동
                .padding(paddingValues) // Scaffold의 패딩 적용 (TopAppBar 높이 등 고려)
                .padding(horizontal = 20.dp), // 기존 좌우 패딩 유지
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer(modifier = Modifier.height(100.dp)) // TopAppBar가 생겼으므로 조정 가능
            // "프로필 등록" 텍스트와 TopAppBar 간의 간격 조정
            Spacer(modifier = Modifier.height(30.dp)) // 예시: 간격 조절

            Text(
                text = "프로필 등록",
                color = Color(0xFF8A8A8A),
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Start) // 이 Modifier는 유지
            )
            Spacer(modifier = Modifier.height(22.dp))

            // 중단: 프로필 사진 + 닉네임 입력
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.user_profile),
                    contentDescription = "프로필 사진"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "닉네임을 알려주세요!",
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        color = Color(0xFF818181)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        singleLine = true,
                        placeholder = { Text("닉네임을 입력해주세요") },
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(7.dp),
                        textStyle = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFD9D9D9),
                            focusedBorderColor = Color(0xFF2466FF),
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            focusedLabelColor = Color(0xFF2466FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // 버튼을 하단으로 밀기 위한 Spacer

            // 등록 버튼
            Button(
                onClick = { onRegister() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // 버튼 하단 여백
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileRegisterScreen() {
    var nickname by remember { mutableStateOf("") }
    SignUpScreen(
        nickname = nickname,
        onNicknameChange = { nickname = it },
        onRegister = {}
    )
}

