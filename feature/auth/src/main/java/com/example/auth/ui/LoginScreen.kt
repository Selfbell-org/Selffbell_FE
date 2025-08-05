package com.example.auth.ui

import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.R
import com.selfbell.core.ui.theme.GrayInactive
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.White
import kotlin.text.all
import kotlin.text.isDigit
import kotlin.text.substring

const val PIN_LENGTH = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateUp: () -> Unit = {},
    onPinCompleted: (String) -> Unit = {} // PIN 입력 완료 시 호출될 콜백
) {
    var pinValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // PIN 값이 변경될 때마다 커서 위치를 항상 마지막으로 이동
    // TextFieldValue를 사용하면 selection을 통해 커서 제어가 용이
    LaunchedEffect(pinValue.text) {
        if (pinValue.text.length > PIN_LENGTH) {
            // 최대 길이 초과 시, 초과된 부분 자르기 (이론상 BasicTextField에서 제한해야 함)
            pinValue = pinValue.copy(text = pinValue.text.substring(0, PIN_LENGTH))
        }
        // 항상 텍스트 끝으로 커서 이동
        pinValue = pinValue.copy(selection = TextRange(pinValue.text.length))

        if (pinValue.text.length == PIN_LENGTH) {
            onPinCompleted(pinValue.text)
            focusManager.clearFocus() // 입력 완료 시 포커스 해제
        }
    }

    // 화면이 처음 나타날 때 BasicTextField에 포커스 요청
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold( // Scaffold로 감싸기
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { // 뒤로가기 버튼
                        Icon(
                            painter = painterResource(R.drawable.backstack_icon),
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // 배경을 투명하게 하거나 앱의 테마에 맞게 설정
                    containerColor = Color.Transparent // 예시: 투명한 배경
                )
            )
        },
        containerColor = Color(0xFFFCFCFF) // 기존 Column의 배경색을 Scaffold로 이동
    ) { paddingValues -> // Scaffold로부터 content padding을 받음
        Column(
            modifier = Modifier
                .fillMaxSize()
                // .background(Color(0xFFFCFCFF)) // 배경색은 Scaffold로 이동
                .padding(paddingValues) // Scaffold의 패딩 적용 (TopAppBar 높이 등 고려)
                .padding(horizontal = 20.dp), // 기존 좌우 패딩 유지
            horizontalAlignment = Alignment.CenterHorizontally,
            // verticalArrangement = Arrangement.Top // 상단 정렬은 유지되나, TopAppBar가 공간 차지
        ) {
            // Spacer(modifier = Modifier.height(100.dp)) // TopAppBar가 있으므로 조정 필요할 수 있음
            // 디자인에 따라 이 Spacer의 높이를 줄이거나, TopAppBar와 컨텐츠 사이 간격을 다르게 조절
            Spacer(modifier = Modifier.height(70.dp)) // 예시: 높이 조절

            Image(
                painter = painterResource(R.drawable.lockkey_icon),
                contentDescription = "Lock Key Icon",
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SelfBell 비밀번호를 입력해주세요.",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            BasicTextField(
                value = pinValue,
                onValueChange = {
                    if (it.text.length <= PIN_LENGTH && it.text.all { char -> char.isDigit() }) {
                        pinValue = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .size(0.dp)
                    .focusRequester(focusRequester),
                decorationBox = {}
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(PIN_LENGTH) { index ->
                    val char = pinValue.text.getOrNull(index)
                    PinBox(
                        hasChar = char != null,
                        isFocused = index == pinValue.text.length && pinValue.text.length < PIN_LENGTH
                        // 마지막 입력칸까지 채워졌을때는 isFocused 해제 (선택적)
                    )
                }
            }
        }
    }
}

@Composable
fun PinBox(
    hasChar: Boolean,
    isFocused: Boolean // 선택적: 현재 입력 포커스를 받은 박스 스타일링
) {
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else GrayInactive

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .background(color = White, shape = RoundedCornerShape(size = 8.dp)),
        contentAlignment = Alignment.Center // 내부 컨텐츠 중앙 정렬
    ) {
        if (hasChar) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    // MaterialTheme.colorScheme.onSurface 또는 직접 정의한 색상 사용
                    .background(
                        MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
            )
        }
    }
}