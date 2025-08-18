package com.selfbell.app.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 새로운 토큰이 생성되거나 기존 토큰이 갱신될 때 호출됩니다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "새로운 토큰 생성: $token")
        // TODO: 서버에 갱신된 토큰을 전송하는 로직을 구현해야 합니다.
    }

    // 푸시 알림 메시지를 받았을 때 호출됩니다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Notification Body: ${it.body}")
        }
    }
}