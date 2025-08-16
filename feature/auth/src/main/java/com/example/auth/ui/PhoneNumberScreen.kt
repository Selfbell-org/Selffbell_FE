package com.example.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfbell.core.R
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Gabarito
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.SelfBellTheme

@Composable
fun PhoneNumberScreen(
    onConfirmClick: (phoneNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    val isPhoneNumberValid = phoneNumber.length == 11

    val currentOnboardingStep = 1
    val totalOnboardingSteps = 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 상단 고정 영역 (온보딩 바) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            OnboardingProgressBar(
                currentStep = currentOnboardingStep,
                totalSteps = totalOnboardingSteps
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- 2. 중앙 콘텐츠 영역 ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // 상단 여백 추가
            Text(
                text = "처음 오셨군요!",
                style = MaterialTheme.typography.headlineMedium, // Pretendard 폰트 사용
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "전화번호로 회원가입을 진행해 주세요.",
                style = MaterialTheme.typography.bodyMedium, // Pretendard 폰트 사용
                textAlign = TextAlign.Center,
                color = Color(0xFF797479)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // 입력 필드
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "전화번호",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newValue ->
                        if (newValue.length <= 11 && newValue.all { it.isDigit() }) {
                            phoneNumber = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("휴대폰번호를 입력하세요") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Primary,
                        unfocusedIndicatorColor = Color(0xFFC7C7C7)
                    )
                )
            }
        }

        // --- 3. 하단 버튼 영역 ---
        SelfBellButton(
            text = "확인",
            onClick = { onConfirmClick(phoneNumber) },
            modifier = Modifier.fillMaxWidth(),
            buttonType = SelfBellButtonType.PRIMARY_FILLED,
            enabled = isPhoneNumberValid
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhoneNumberScreen() {
    SelfBellTheme {
        PhoneNumberScreen(onConfirmClick = {})
    }
}