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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.R
import com.selfbell.core.ui.theme.GreyscaleGrey200
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.White
import kotlin.text.all
import kotlin.text.isDigit
import kotlin.text.substring

const val PIN_LENGTH = 4

@Composable
fun LoginScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFF))
            .padding(horizontal = 20.dp, vertical = 70.dp), // 좌우 패딩 추가
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Image(
            painter = painterResource(R.drawable.lockkey_icon),
            contentDescription = "Lock Key Icon",
            modifier = Modifier.size(60.dp) // 아이콘 크기 조절
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SelfBell 비밀번호를 입력해주세요.",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp // 폰트 크기 조절
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 숨겨진 BasicTextField: 실제 입력 처리
        BasicTextField(
            value = pinValue,
            onValueChange = {
                if (it.text.length <= PIN_LENGTH && it.text.all { char -> char.isDigit() }) {
                    pinValue = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .size(0.dp) // 화면에 보이지 않도록 크기를 0으로 설정
                .focusRequester(focusRequester),
            decorationBox = {
                // 이 부분은 보이지 않으므로 비워둠
            }
        )

        // PIN 입력 칸 UI
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically // 내부 요소들 수직 중앙 정렬
        ) {
            repeat(PIN_LENGTH) { index ->
                val char = pinValue.text.getOrNull(index)
                PinBox(
                    hasChar = char != null,
                    isFocused = index == pinValue.text.length // 현재 입력될 차례의 박스에 포커스 효과 (선택적)
                )
            }
        }
        // 여기에 숫자 키패드 Composable을 추가하거나,
        // 시스템 키보드가 올라오도록 BasicTextField에 포커스를 줍니다.
        // 현재는 BasicTextField에 포커스를 주어 시스템 숫자 키패드를 사용합니다.
    }
}

@Composable
fun PinBox(
    hasChar: Boolean,
    isFocused: Boolean // 선택적: 현재 입력 포커스를 받은 박스 스타일링
) {
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else GreyscaleGrey200
    // MaterialTheme을 사용하지 않는다면 직접 Color 정의
    // val focusedBorderColor = Color.Blue // 예시

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


@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen()
}