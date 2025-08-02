package com.selfbell.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// TODO: 커스텀 폰트를 app/res/font 폴더에 추가했다면 아래 주석을 해제하고 R 임포트
// import com.selfbell.R

// Figma에 명시된 폰트 패밀리 정의 [cite: image.png]
val Pretendard = FontFamily(
    // TODO: app/res/font에 Pretendard 폰트 파일이 있다면 아래 라인을 R.font.pretendard_regular 등으로 교체
    // Font(R.font.pretendard_regular, FontWeight.Normal),
    // Font(R.font.pretendard_medium, FontWeight.Medium),
    // Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    // Font(R.font.pretendard_bold, FontWeight.Bold)
)
val Gabarito = FontFamily(
    // TODO: app/res/font에 Gabarito 폰트 파일이 있다면 아래 라인을 R.font.gabarito_variablefont_wght 등으로 교체
    // Font(R.font.gabarito_variablefont_wght, FontWeight.W700)
)

// 만약 폰트 파일이 없다면 일단 시스템 기본 폰트로 대체
val DefaultPretendard = FontFamily.Default
val DefaultGabarito = FontFamily.Default

val Typography = Typography(
    // Header, Pretendard, Semibold, 24 [cite: image.png]
    headlineMedium = TextStyle(
        fontFamily = DefaultPretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    // Title, Pretendard, Semibold, 18 [cite: image.png]
    titleMedium = TextStyle(
        fontFamily = DefaultPretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    // Body, Pretendard, Medium, 14 [cite: image.png]
    bodyMedium = TextStyle(
        fontFamily = DefaultPretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // Caption, Pretendard, Medium, 14 [cite: image.png]
    labelSmall = TextStyle( // Material3의 labelSmall을 Figma의 Caption에 대응
        fontFamily = DefaultPretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // 스플래시 화면용 타이포그래피 추가 (Gabarito, 40)
    displayLarge = TextStyle(
        fontFamily = DefaultGabarito,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = 0.5.sp
    )
)