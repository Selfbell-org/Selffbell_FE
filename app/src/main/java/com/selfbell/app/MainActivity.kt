package com.selfbell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
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
                // rememberNavController()를 사용하여 NavController 인스턴스 생성
                val navController = rememberNavController()
                // 앱의 최상위 내비게이션을 AppNavHost로 관리
                AppNavHost(navController = navController)
            }
        }
    }
}

// Greeting Composable은 이제 AppNavHost 외부에서 사용되지 않으므로 제거 가능 (선택 사항)
/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SelfBellTheme {
        Greeting("Android")
    }
}
*/