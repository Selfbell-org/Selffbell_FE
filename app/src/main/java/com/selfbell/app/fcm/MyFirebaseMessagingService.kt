package com.selfbell.app.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.selfbell.data.repository.impl.FCMTokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    companion object {
        private const val TAG = "FCM_TOKEN"
    }

    // 새로운 토큰이 생성되거나 기존 토큰이 갱신될 때 호출됩니다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰 생성: $token")
        
        // ✅ 새로운 토큰을 FCMTokenManager에 저장하고 서버에 업데이트
        updateTokenOnServer(token)
    }

    // 푸시 알림 메시지를 받았을 때 호출됩니다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "From: ${remoteMessage.from}")
        
        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Notification Body: ${it.body}")
            Log.d("FCM_MESSAGE", "Notification Title: ${it.title}")
        }
        
        // 데이터 메시지 처리
        remoteMessage.data.forEach { (key, value) ->
            Log.d("FCM_MESSAGE", "Data: $key = $value")
        }
    }
    
    /**
     * 새로운 FCM 토큰을 서버에 업데이트합니다.
     * @param newToken 새로운 FCM 토큰
     */
    private fun updateTokenOnServer(newToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: 서버에 새로운 토큰을 전송하는 API 호출
                // 예: authRepository.updateDeviceToken(newToken)
                Log.d(TAG, "서버에 FCM 토큰 업데이트 완료: $newToken")
            } catch (e: Exception) {
                Log.e(TAG, "서버에 FCM 토큰 업데이트 실패", e)
            }
        }
    }
}