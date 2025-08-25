package com.selfbell.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
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
import android.content.Intent
import android.app.PendingIntent
import com.selfbell.app.MainActivity
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== MyFirebaseMessagingService onCreate 호출됨 ===")
        
        // 현재 FCM 토큰 확인 및 서버 업데이트
        checkAndUpdateFCMToken()
    }

    // 새로운 토큰이 생성되거나 기존 토큰이 갱신될 때 호출됩니다.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰 생성: $token")

        // 새로운 토큰을 FCMTokenManager에 저장하고 서버에 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fcmTokenManager.saveFCMToken(token)
                Log.d(TAG, "FCM 토큰 로컬 저장 완료")
                
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
            
            // 메시지 타입에 따른 분기 처리
            val messageType = data["type"]
            when (messageType) {
                "SAFE_WALK_STARTED", "SAFE_WALK_ENDED" -> {
                    // 안심귀가 관련 알림 처리
                    Log.d(TAG, "🚶‍♀️ 안심귀가 알림 타입 감지: $messageType")
                    handleSafeWalkNotification(data)
                }
                "SOS_MESSAGE", "SOS_EMERGENCY" -> {
                    // SOS 긴급신고 알림 처리
                    Log.d(TAG, "🚨 SOS 긴급신고 알림 타입 감지: $messageType")
                    handleSOSNotification(data)
                }
                else -> {
                    Log.w(TAG, "⚠️ 알 수 없는 메시지 타입: $messageType")
                    Log.d(TAG, "전체 데이터: $data")
                }
            }
        }
        
        Log.d(TAG, "=== FCM 메시지 수신 완료 ===")
    }

    /**
     * FCM 토큰을 확인하고 서버에 업데이트하는 함수
     */
    private fun checkAndUpdateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "현재 FCM 토큰: $token")
                
                if (!token.isNullOrEmpty()) {
                    // 토큰이 있으면 서버에 업데이트 시도
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            fcmTokenManager.saveFCMToken(token)
                            Log.d(TAG, "onCreate에서 FCM 토큰 로컬 저장 완료")
                            
                            // 서버 업데이트 시도 (최대 3번)
                            var updateSuccess = false
                            repeat(3) { attempt ->
                                try {
                                    Log.d(TAG, "FCM 토큰 서버 업데이트 시도 ${attempt + 1}/3")
                                    authRepository.updateDeviceToken(token)
                                    Log.d(TAG, "onCreate에서 FCM 토큰 서버 업데이트 완료")
                                    updateSuccess = true
                                    return@repeat
                                } catch (e: Exception) {
                                    Log.e(TAG, "FCM 토큰 서버 업데이트 시도 ${attempt + 1}/3 실패", e)
                                    if (attempt < 2) {
                                            delay(1000L * (attempt + 1)) // 1초, 2초 대기
                                    }
                                }
                            }
                            
                            if (!updateSuccess) {
                                Log.e(TAG, "FCM 토큰 서버 업데이트 최종 실패")
                            }
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "onCreate에서 FCM 토큰 서버 업데이트 실패", e)
                        }
                    }
                } else {
                    Log.w(TAG, "FCM 토큰이 null 또는 빈 문자열입니다")
                }
            } else {
                Log.e(TAG, "FCM 토큰 가져오기 실패", task.exception)
            }
        }
    }

    /**
     * FCM 토큰을 강제로 서버에 업데이트하는 함수
     * 앱 시작 시나 필요할 때 호출
     */
    fun forceUpdateFCMToken() {
        Log.d(TAG, "=== FCM 토큰 강제 업데이트 시작 ===")
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "강제 업데이트용 FCM 토큰: $token")
                
                if (!token.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // 로컬 저장
                            fcmTokenManager.saveFCMToken(token)
                            Log.d(TAG, "강제 업데이트: FCM 토큰 로컬 저장 완료")
                            
                            // 서버 업데이트
                            authRepository.updateDeviceToken(token)
                            Log.d(TAG, "강제 업데이트: FCM 토큰 서버 업데이트 완료")
                            
                            // 토큰 유효성 확인
                            Log.d(TAG, "✅ FCM 토큰이 서버에 성공적으로 등록되었습니다")
                            Log.d(TAG, "토큰: ${token.take(20)}...${token.takeLast(20)}")
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "강제 업데이트: FCM 토큰 서버 업데이트 실패", e)
                        }
                    }
                } else {
                    Log.e(TAG, "강제 업데이트: FCM 토큰이 null입니다")
                }
            } else {
                Log.e(TAG, "강제 업데이트: FCM 토큰 가져오기 실패", task.exception)
            }
        }
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
     * SOS 긴급신고 알림 처리
     */
    private fun handleSOSNotification(data: Map<String, String>) {
        Log.d(TAG, "🚨 SOS 긴급신고 알림 처리 시작")
        
        try {
            val senderName = data["senderName"] ?: "알 수 없음"
            val message = data["message"] ?: "긴급 신고가 발생했습니다"
            val lat = data["lat"]?.toDoubleOrNull()
            val lon = data["lon"]?.toDoubleOrNull()
            
            Log.d(TAG, "📋 SOS 알림 데이터:")
            Log.d(TAG, "  - 발신자: $senderName")
            Log.d(TAG, "  - 메시지: $message")
            Log.d(TAG, "  - 위치: lat=$lat, lon=$lon")
            
            // 시스템 알림 생성
            createSOSNotification(senderName, message, lat, lon)
            
            Log.d(TAG, "✅ SOS 긴급신고 알림 처리 완료")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ SOS 긴급신고 알림 처리 실패", e)
        }
    }

    /**
     * SOS 긴급신고 시스템 알림 생성
     */
    private fun createSOSNotification(
        senderName: String,
        message: String,
        lat: Double?,
        lon: Double?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 알림 채널 생성 (Android O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sos_emergency_channel",
                "긴급신고",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급신고 알림"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(this, "sos_emergency_channel")
            .setSmallIcon(CoreR.drawable.default_profile_icon2)
            .setContentTitle("긴급신고")
            .setContentText("${senderName}으로부터 온 긴급 신고입니다. 연락 바랍니다")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${senderName}으로부터 온 긴급 신고입니다. 연락 바랍니다"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // 긴급신고용 진동 패턴
        
        // 위치 정보가 있으면 클릭 시 위치 화면으로 이동하는 Intent 추가
        if (lat != null && lon != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SOS_EMERGENCY", true)
                putExtra("SOS_LAT", lat)
                putExtra("SOS_LON", lon)
                putExtra("SOS_SENDER_NAME", senderName)
                putExtra("SOS_MESSAGE", message)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            notificationBuilder.setContentIntent(pendingIntent)
            
            // 위치 확인 액션 버튼 추가
            notificationBuilder.addAction(
                CoreR.drawable.default_profile_icon2,
                "위치 확인",
                pendingIntent
            )
        }
        
        // 알림 표시
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "🔔 SOS 긴급신고 시스템 알림 생성 완료")
        Log.d(TAG, "  - 제목: 긴급신고")
        Log.d(TAG, "  - 본문: ${senderName}으로부터 온 긴급 신고입니다. 연락 바랍니다")
        Log.d(TAG, "  - 알림 ID: $notificationId")
        Log.d(TAG, "  - 위치 정보: lat=$lat, lon=$lon")
        Log.d(TAG, "  - 클릭 시 위치 화면으로 이동 설정됨")
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
