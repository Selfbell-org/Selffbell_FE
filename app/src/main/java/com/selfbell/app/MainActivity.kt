package com.selfbell.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.enableEdgeToEdge // 전체 화면 사용을 위한 유틸리티
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import com.selfbell.app.ui.SplashScreen
import com.selfbell.core.ui.theme.SelfBellTheme
import com.selfbell.app.navigation.AppNavHost // AppNavHost 임포트
import androidx.navigation.compose.rememberNavController // rememberNavController 임포트
import com.google.firebase.messaging.FirebaseMessaging
import com.selfbell.app.fcm.MyFirebaseMessagingService

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: 스플래시 화면 설치 (친구 분의 작업)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // FCM 토큰 확인 테스트
        checkFCMToken()
        
        // FCM 토큰 강제 업데이트 (보호자에게 알림이 가도록)
        forceUpdateFCMToken()
        
        // SOS 긴급신고 Intent 처리
        handleSOSEmergencyIntent(intent)
        
        setContent {
            SelfBellTheme {
                // NavController는 앱의 최상위 내비게이션을 관리하므로 여기서 생성하고 AppNavHost에 전달
                val navController = rememberNavController() // <-- 여기서 NavController 생성
                AppNavHost(navController = navController) // <-- AppNavHost를 호출
            }
        }
    }
    
    /**
     * FCM 토큰 상태를 확인하는 함수
     */
    private fun checkFCMToken() {
        Log.d(TAG, "=== FCM 토큰 확인 시작 ===")
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM 토큰 확인 성공: $token")
                
                if (!token.isNullOrEmpty()) {
                    Log.d(TAG, "FCM 토큰이 정상적으로 생성되었습니다")
                } else {
                    Log.w(TAG, "FCM 토큰이 null 또는 빈 문자열입니다")
                }
            } else {
                Log.e(TAG, "FCM 토큰 확인 실패", task.exception)
            }
        }
    }
    
    /**
     * FCM 서비스를 강제로 시작하여 테스트하는 함수
     */
    private fun testFCMService() {
        Log.d(TAG, "=== FCM 서비스 강제 시작 테스트 ===")
        
        try {
            // FCM 서비스를 강제로 시작
            val intent = Intent(this, MyFirebaseMessagingService::class.java)
            startService(intent)
            Log.d(TAG, "FCM 서비스 강제 시작 요청 완료")
        } catch (e: Exception) {
            Log.e(TAG, "FCM 서비스 강제 시작 실패", e)
        }
    }

    /**
     * FCM 토큰 강제 업데이트 (보호자에게 알림이 가도록)
     */
    private fun forceUpdateFCMToken() {
        Log.d(TAG, "=== FCM 토큰 강제 업데이트 시작 ===")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM 토큰 강제 업데이트 성공: $token")
                if (!token.isNullOrEmpty()) {
                    Log.d(TAG, "FCM 토큰이 정상적으로 생성되었습니다")
                    Log.d(TAG, "토큰 길이: ${token.length}")
                    Log.d(TAG, "토큰 시작: ${token.take(20)}...")
                    Log.d(TAG, "토큰 끝: ...${token.takeLast(20)}")
                    
                    // 토큰 유효성 확인
                    if (token.length > 100) {
                        Log.d(TAG, "✅ FCM 토큰 형식이 올바릅니다")
                    } else {
                        Log.w(TAG, "⚠️ FCM 토큰 길이가 비정상적입니다")
                    }
                } else {
                    Log.w(TAG, "FCM 토큰이 null 또는 빈 문자열입니다")
                }
            } else {
                Log.e(TAG, "FCM 토큰 강제 업데이트 실패", task.exception)
            }
        }
    }

    /**
     * SOS 긴급신고 Intent 처리
     */
    private fun handleSOSEmergencyIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("SOS_EMERGENCY", false) == true) {
            val lat = intent.getDoubleExtra("SOS_LAT", 0.0)
            val lon = intent.getDoubleExtra("SOS_LON", 0.0)
            val senderName = intent.getStringExtra("SOS_SENDER_NAME") ?: "알 수 없음"
            val message = intent.getStringExtra("SOS_MESSAGE") ?: ""
            
            Log.d(TAG, "🚨 SOS 긴급신고 Intent 감지: $senderName, 위치: $lat, $lon")
            
            // TODO: SOS 위치 화면으로 네비게이션
            // 현재는 로그만 출력, 나중에 네비게이션 로직 추가 예정
            Log.d(TAG, "�� SOS 위치 화면으로 이동 예정: ${senderName}님의 긴급신고")
            Log.d(TAG, "📍 위치: 위도 $lat, 경도 $lon")
            Log.d(TAG, "📍 메시지: $message")
        }
    }
}
