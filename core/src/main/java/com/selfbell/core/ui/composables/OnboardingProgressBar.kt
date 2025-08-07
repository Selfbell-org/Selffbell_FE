package com.selfbell.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfbell.core.ui.theme.Primary
import com.selfbell.core.ui.theme.GrayInactive as Gray100
import com.selfbell.core.ui.theme.SelfBellTheme

@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..totalSteps).forEach { step ->
            // 단계 원형 아이콘
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (step <= currentStep) Primary else Gray100), // 현재 단계까지 Primary 색상 적용
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 연결선 (마지막 원형에는 추가하지 않음)
            if (step < totalSteps) {
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .width(28.dp) // 연결선의 길이
                        .background(if (step < currentStep) Primary else Gray100) // 현재 단계 이전까지 Primary 색상 적용
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingProgressBarPreview() {
    SelfBellTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            OnboardingProgressBar(currentStep = 1, totalSteps = 4)
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingProgressBar(currentStep = 2, totalSteps = 4)
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingProgressBar(currentStep = 3, totalSteps = 4)
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingProgressBar(currentStep = 4, totalSteps = 4)
        }
    }
}
