
package com.selfbell.app

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp // HiltAndroidApp 어노테이션 임포트

/**
 * SelfBell 애플리케이션의 Hilt 진입점.
 * @HiltAndroidApp 어노테이션은 Hilt가 이 Application 클래스를 사용하여
 * 앱의 의존성 그래프를 생성하도록 지시합니다.
 */
@HiltAndroidApp
class SelfBellApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val clientId = getString(R.string.naver_maps_client_id_from_gradle) // 생성한 리소스 ID 사용
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient(clientId)
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM_TOKEN", "FCM 토큰 가져오기 실패", task.exception)
//                return@addOnCompleteListener
//            }
//            val token = task.result
//            Log.d("FCM_TOKEN", "FCM 토큰: $token")
//        }
    }
}
