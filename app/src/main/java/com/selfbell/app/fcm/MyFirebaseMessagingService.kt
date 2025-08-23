package com.selfbell.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.selfbell.core.R
import com.selfbell.data.repository.impl.FCMTokenManager
import com.selfbell.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.selfbell.core.R as CoreR // ✅ App 모듈의 R 파일 import

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    @Inject
    lateinit var authRepository: AuthRepository // ✅ AuthRepository 주입

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    // 새로운 토큰이 생성되거나 기존 토큰이 갱신될 때 호출됩니다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰 생성: $token")

        // ✅ 새로운 토큰을 FCMTokenManager에 저장하고 서버에 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fcmTokenManager.saveFCMToken(token)
                authRepository.updateDeviceToken(token)
                Log.d(TAG, "서버에 FCM 토큰 업데이트 완료: $token")
            } catch (e: Exception) {
                Log.e(TAG, "서버에 FCM 토큰 업데이트 실패", e)
            }
        }
    }

    // 푸시 알림 메시지를 받았을 때 호출됩니다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 알림 페이로드(Notification Payload)가 있는 경우
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification Title: ${notification.title}")
            Log.d(TAG, "Notification Body: ${notification.body}")

            // ✅ 알림 메시지를 사용자에게 표시하는 함수 호출
            showNotification(notification.title, notification.body)
        }

        // 데이터 페이로드(Data Payload) 처리
        remoteMessage.data.let { data ->
            Log.d(TAG, "메시지 데이터 페이로드: $data")
            // TODO: 수신된 데이터를 바탕으로 앱 내부 로직을 처리 (예: 긴급 상황 처리)
        }
    }

    /**
     * 알림을 표시하는 함수.
     * @param title 알림 제목
     * @param body 알림 본문
     */
    private fun showNotification(title: String?, body: String?) {
        val channelId = "emergency_alert_channel"

        // 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.default_profile_icon2) // ✅ App 모듈의 알림 아이콘
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 높은 우선순위
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0(Oreo) 이상에서는 알림 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "긴급 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급 신고 알림입니다."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 표시
        notificationManager.notify(0 /* 알림 ID */, notificationBuilder.build())
    }
}