
package com.selfbell.app

import android.app.Application
import android.util.Log
import com.naver.maps.map.NaverMapSdk
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

/**
 * SelfBell 애플리케이션의 Hilt 진입점.
 * @HiltAndroidApp 어노테이션은 Hilt가 이 Application 클래스를 사용하여
 * 앱의 의존성 그래프를 생성하도록 지시합니다.
 */
@HiltAndroidApp
class SelfBellApplication : Application() {
    
    companion object {
        private const val TAG = "SelfBellApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Naver Maps SDK 초기화
        val clientId = getString(R.string.naver_maps_client_id_from_gradle)
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NcpKeyClient(clientId)
        
        // ✅ FCM 토큰 초기화
        initializeFCMToken()
    }
    
    /**
     * FCM 토큰을 초기화하고 로깅합니다.
     */
    private fun initializeFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "FCM 토큰 초기화 성공: $token")
                } else {
                    Log.e(TAG, "FCM 토큰 초기화 실패", task.exception)
                }
        }
    }
}
