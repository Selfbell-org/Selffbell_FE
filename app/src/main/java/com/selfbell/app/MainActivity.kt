package com.selfbell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.enableEdgeToEdge // 전체 화면 사용을 위한 유틸리티
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.selfbell.app.ui.SplashScreen
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.app.navigation.AppNavHost // AppNavHost 임포트
import androidx.navigation.compose.rememberNavController // rememberNavController 임포트

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: 스플래시 화면 설치 (친구 분의 작업)
        // val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelfBellTheme {
                // NavController는 앱의 최상위 내비게이션을 관리하므로 여기서 생성하고 AppNavHost에 전달
                val navController = rememberNavController() // <-- 여기서 NavController 생성
                AppNavHost(navController = navController) // <-- AppNavHost를 호출
            }
        }
    }
}
