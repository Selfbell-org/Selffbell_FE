package com.selfbell.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.selfbell.core.R // <-- core 모듈의 R 클래스 임포트

// Figma에 명시된 Pretendard 폰트 패밀리 정의
// 이 폰트 파일들이 core/src/main/res/font/ 폴더에 있다고 가정
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal), // 예시: pretendard_regular.ttf
    Font(R.font.pretendard_medium, FontWeight.Medium), // 예시: pretendard_medium.ttf
    Font(R.font.pretendard_semibold, FontWeight.SemiBold), // 예시: pretendard_semibold.ttf
    Font(R.font.pretendard_bold, FontWeight.Bold) // 예시: pretendard_bold.ttf (필요하다면)
)

// Gabarito 폰트 패밀리 정의
// 이 폰트 파일이 core/src/main/res/font/ 폴더에 있다고 가정
val Gabarito = FontFamily(
    Font(R.font.gabarito_variablefont_wght, FontWeight.W700) // 예시: gabarito_variablefont_wght.ttf
)

val Typography = Typography(
    // Header, Pretendard, Semibold, 24
    headlineMedium = TextStyle( // <-- 이 부분이 정확히 정의되어 있어야 합니다.
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold, // Weight: 600
        fontSize = 24.sp, // Size: 24px
        lineHeight = 24.sp * 1.4f, // Line height: 140%
        letterSpacing = 0.sp // Letter spacing: 0%
    ),
    // Title, Pretendard, Semibold, 18
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    // Body, Pretendard, Medium, 14
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // Caption, Pretendard, Medium, 14
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // 스플래시 화면용 타이포그래피 (Gabarito, 40)
    displayLarge = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Bold, // Gabarito 폰트의 700에 해당
        fontSize = 40.sp,
        letterSpacing = 0.5.sp
    )
)