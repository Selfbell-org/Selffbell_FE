package com.example.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfbell.core.R
import com.selfbell.core.ui.composables.OnboardingProgressBar
import com.selfbell.core.ui.composables.SelfBellButton
import com.selfbell.core.ui.composables.SelfBellButtonType
import com.selfbell.core.ui.theme.Pretendard
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.SelfBellTheme

@Composable
fun PasswordScreen(
    phoneNumber: String,
    onConfirmClick: (password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }

    val isLengthValid = password.length in 8..20
    val isContentValid = password.any { it.isLetter() } && password.any { it.isDigit() }

    val isPasswordValid = isLengthValid && isContentValid

    val currentOnboardingStep = 1 // Corrected to 2 for this screen
    val totalOnboardingSteps = 4

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
            OnboardingProgressBar(
                currentStep = currentOnboardingStep,
                totalSteps = totalOnboardingSteps
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- 2. Central Content Area ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "비밀번호를 입력해 주세요.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "따옴표, 괄호, 세미콜론 등은 사용할 수 없어요.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF797479)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Password Input Field
            Text(
                text = "비밀번호",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = { Text("영문, 숫자, 특수문자 포함 8자 이상") },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Primary,
                    unfocusedIndicatorColor = Color(0xFFC7C7C7)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password Validation Checks
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                ValidationCheckText(
                    text = "8-20자 이내",
                    isValid = isLengthValid
                )
                Spacer(modifier = Modifier.height(4.dp))
                ValidationCheckText(
                    text = "영문 대소문자, 숫자 포함",
                    isValid = isContentValid
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Previous Screen's Phone Number
            Text(
                text = "전화번호",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {}, // Read-only
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Disable user input
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledIndicatorColor = Color(0xFFC7C7C7),
                    disabledTextColor = Color.Black
                )
            )
        }

        // --- 3. Bottom Button Area ---
        SelfBellButton(
            text = "확인",
            onClick = { onConfirmClick(password) },
            modifier = Modifier.fillMaxWidth(),
            buttonType = SelfBellButtonType.PRIMARY_FILLED,
            enabled = isPasswordValid
        )
    }
}

@Composable
fun ValidationCheckText(text: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (isValid) Primary else Color(0xFFC7C7C7),
            modifier = Modifier.height(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = if (isValid) Primary else Color(0xFF797479),
            style = MaterialTheme.typography.bodyMedium // Using BodyMedium for consistent style
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPasswordScreen() {
    SelfBellTheme {
        PasswordScreen(phoneNumber = "01012341233", onConfirmClick = {})
    }
}