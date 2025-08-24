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
import com.selfbell.core.R as CoreR

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    // 새로운 토큰이 생성되거나 기존 토큰이 갱신될 때 호출됩니다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰 생성: $token")

        // 새로운 토큰을 FCMTokenManager에 저장하고 서버에 업데이트
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
        Log.d(TAG, "=== FCM 메시지 수신 시작 ===")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Sent Time: ${remoteMessage.sentTime}")
        Log.d(TAG, "TTL: ${remoteMessage.ttl}")

        // 알림 페이로드(Notification Payload)가 있는 경우
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "📱 Notification Payload 감지")
            Log.d(TAG, "Notification Title: ${notification.title}")
            Log.d(TAG, "Notification Body: ${notification.body}")
            Log.d(TAG, "Notification Icon: ${notification.icon}")
            Log.d(TAG, "Notification Color: ${notification.color}")

            // 알림 메시지를 사용자에게 표시하는 함수 호출
            showNotification(notification.title, notification.body)
        }

        // 데이터 페이로드(Data Payload) 처리
        remoteMessage.data.let { data ->
            Log.d(TAG, "📊 Data Payload 감지")
            Log.d(TAG, "데이터 개수: ${data.size}")
            data.forEach { (key, value) ->
                Log.d(TAG, "  $key: $value")
            }
            
            // 안심귀가 관련 알림 처리
            handleSafeWalkNotification(data)
        }
        
        Log.d(TAG, "=== FCM 메시지 수신 완료 ===")
    }

    /**
     * 안심귀가 관련 FCM 알림을 처리하는 함수
     * @param data FCM 데이터 페이로드
     */
    private fun handleSafeWalkNotification(data: Map<String, String>) {
        Log.d(TAG, "🔍 안심귀가 알림 처리 시작")
        
        val type = data["type"]
        val wardName = data["wardName"] ?: "보호 대상자"
        val message = data["message"] ?: ""
        val isGuardian = data["isGuardian"]?.toBoolean() ?: false
        
        Log.d(TAG, "📋 파싱된 데이터:")
        Log.d(TAG, "  - type: $type")
        Log.d(TAG, "  - wardName: $wardName")
        Log.d(TAG, "  - message: $message")
        Log.d(TAG, "  - isGuardian: $isGuardian")
        
        when (type) {
            "SAFE_WALK_STARTED" -> {
                Log.d(TAG, "🚶‍♀️ 안심귀가 시작 알림 감지")
                Log.d(TAG, "보호 대상자: $wardName")
                Log.d(TAG, "알림 메시지: $message")
                Log.d(TAG, "수신자: ${if (isGuardian) "보호자" else "피보호자"}")
                
                val notificationBody = if (isGuardian) {
                    "${wardName}님의 안전 귀가가 시작되었습니다"
                } else {
                    "안전 귀가가 시작되었습니다"
                }
                
                showSafeWalkNotification(
                    title = "안심귀가 시작",
                    body = notificationBody,
                    isStart = true
                )
            }
            "SAFE_WALK_ENDED" -> {
                Log.d(TAG, "🏁 안심귀가 종료 알림 감지")
                Log.d(TAG, "보호 대상자: $wardName")
                Log.d(TAG, "알림 메시지: $message")
                Log.d(TAG, "수신자: ${if (isGuardian) "보호자" else "피보호자"}")
                
                val notificationBody = if (isGuardian) {
                    "${wardName}님의 안전 귀가가 종료되었습니다"
                } else {
                    "안전 귀가가 종료되었습니다"
                }
                
                showSafeWalkNotification(
                    title = "안심귀가 종료",
                    body = notificationBody,
                    isStart = false
                )
            }
            else -> {
                Log.w(TAG, "⚠️ 알 수 없는 안심귀가 알림 타입: $type")
                Log.d(TAG, "전체 데이터: $data")
            }
        }
        
        Log.d(TAG, "✅ 안심귀가 알림 처리 완료")
    }

    /**
     * 안심귀가 알림을 표시하는 함수
     * @param title 알림 제목
     * @param body 알림 본문
     * @param isStart 시작 알림 여부
     */
    private fun showSafeWalkNotification(title: String, body: String, isStart: Boolean) {
        Log.d(TAG, "🔔 안심귀가 시스템 알림 생성 시작")
        Log.d(TAG, "  - 제목: $title")
        Log.d(TAG, "  - 본문: $body")
        Log.d(TAG, "  - 시작 알림 여부: $isStart")
        
        val channelId = "safe_walk_channel"
        val notificationId = if (isStart) 1001 else 1002 // 시작/종료 구분을 위한 ID
        
        Log.d(TAG, "  - 채널 ID: $channelId")
        Log.d(TAG, "  - 알림 ID: $notificationId")

        // 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.default_profile_icon2)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        Log.d(TAG, "  - 알림 빌더 생성 완료")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0(Oreo) 이상에서는 알림 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "  - Android O 이상: 알림 채널 생성 시도")
            val channel = NotificationChannel(
                channelId,
                "안심귀가 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "안심귀가 시작/종료 알림입니다."
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "  - 알림 채널 생성 완료: $channelId")
        } else {
            Log.d(TAG, "  - Android O 미만: 알림 채널 불필요")
        }

        // 알림 표시
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "✅ 안심귀가 시스템 알림 표시 성공")
            Log.d(TAG, "  - 최종 알림 ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 안심귀가 시스템 알림 표시 실패", e)
        }
    }

    /**
     * 알림을 표시하는 함수.
     * @param title 알림 제목
     * @param body 알림 본문
     */
    private fun showNotification(title: String?, body: String?) {
        Log.d(TAG, "🚨 긴급 알림 시스템 알림 생성 시작")
        Log.d(TAG, "  - 제목: $title")
        Log.d(TAG, "  - 본문: $body")
        
        val channelId = "emergency_alert_channel"
        Log.d(TAG, "  - 채널 ID: $channelId")

        // 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.default_profile_icon2)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        Log.d(TAG, "  - 긴급 알림 빌더 생성 완료")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0(Oreo) 이상에서는 알림 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "  - Android O 이상: 긴급 알림 채널 생성 시도")
            val channel = NotificationChannel(
                channelId,
                "긴급 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급 신고 알림입니다."
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "  - 긴급 알림 채널 생성 완료: $channelId")
        } else {
            Log.d(TAG, "  - Android O 미만: 긴급 알림 채널 불필요")
        }

        // 알림 표시
        try {
            notificationManager.notify(0, notificationBuilder.build())
            Log.d(TAG, "✅ 긴급 알림 시스템 알림 표시 성공")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 긴급 알림 시스템 알림 표시 실패", e)
        }
    }
}
