package com.selfbell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // 전체 화면 사용을 위한 유틸리티
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold // Material3 Scaffold (기본 레이아웃 구조 제공)
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.selfbell.app.ui.SplashScreen
import com.selfbell.core.ui.theme.SelfBellTheme
// core 모듈에서 정의된 테마 임포트 (패키지 구조에 따라 다를 수 있음)
//import com.selfbell.core.ui.theme.SelfBellTheme
import dagger.hilt.android.AndroidEntryPoint // AndroidEntryPoint 어노테이션 임포트

/**
 * SelfBell 앱의 메인 활동 (Activity).
 * @AndroidEntryPoint 어노테이션은 Hilt가 이 Activity에 의존성을 주입할 수 있도록 합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면을 시스템 바(상태바, 내비게이션 바) 영역까지 확장
        enableEdgeToEdge()
        setContent {
            // core 모듈에서 정의된 앱 테마 적용
            SelfBellTheme {
                // Scaffold는 기본 Material Design 레이아웃 구조를 제공
                SplashScreen()
            }
        }
    }
}

/**
 * 간단한 환영 메시지를 표시하는 Composable 함수.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * Greeting Composable의 미리보기 함수.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SelfBellTheme {
        Greeting("Android")
    }
}