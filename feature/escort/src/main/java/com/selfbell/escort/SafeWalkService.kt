// feature/escort/src/main/java/com/selfbell/escort/ui/SafeWalkService.kt
package com.selfbell.escort.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
// import com.selfbell.app.ui.MainActivity
import com.selfbell.core.R
import com.selfbell.core.location.LocationTracker
import com.selfbell.data.api.StompManager
import com.selfbell.domain.repository.SafeWalkRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SafeWalkService : Service() {

    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var safeWalkRepository: SafeWalkRepository
    @Inject
    lateinit var stompManager: StompManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var sessionId: Long = -1

    companion object {
        private const val CHANNEL_ID = "safe_walk_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SafeWalkService", "SafeWalkService 시작")
        sessionId = intent?.getLongExtra("SESSION_ID", -1L) ?: -1L

        if (sessionId == -1L) {
            Log.e("SafeWalkService", "세션 ID가 없어 서비스를 종료합니다.")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundService()
        // ✅ 위치 추적 시작 후 주기적 위치 전송 시작
        startLocationTracking()
        startPeriodicLocationReporting()

        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationIntent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("안심 귀가 서비스")
            .setContentText("안심 귀가 중입니다. 목적지까지 안전하게 이동하세요.")
            .setSmallIcon(R.drawable.default_profile_icon2)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "안심 귀가 채널",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "안심 귀가 서비스 알림"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    // ✅ 위치 추적을 시작하는 함수
    private fun startLocationTracking() {
        Log.d("SafeWalkService", "위치 추적 시작")
        // LocationTracker의 위치 업데이트를 시작하여 초기 위치를 얻음
        serviceScope.launch {
            locationTracker.getLocationUpdates()
                .collect { location ->
                    Log.d("SafeWalkService", "위치 업데이트 수신: lat=${location.latitude}, lon=${location.longitude}")
                }
        }
    }

    // ✅ 수정됨: 1분(60초)마다 독립적으로 위치를 전송하는 함수
    private fun startPeriodicLocationReporting() {
        serviceScope.launch {
            while (isActive) {
                // ✅ 개선된 위치 획득 메서드 사용
                val location = locationTracker.getLastKnownLocationWithLogging()
                if (location != null) {
                    try {
                        safeWalkRepository.uploadLocationTrack(
                            sessionId = sessionId,
                            lat = location.latitude,
                            lon = location.longitude,
                            accuracy = location.accuracy.toDouble()
                        )
                        stompManager.sendLocation(
                            sessionId = sessionId,
                            lat = location.latitude,
                            lon = location.longitude
                        )
                        Log.d("SafeWalkService", "위치 정보 서버 전송 성공: lat=${location.latitude}, lon=${location.longitude}")
                    } catch (e: Exception) {
                        Log.e("SafeWalkService", "위치 정보 서버 전송 실패", e)
                    }
                } else {
                    Log.w("SafeWalkService", "마지막으로 알려진 위치를 가져오지 못했습니다.")
                }
                // 10초 대기 (테스트용으로 단축)
                delay(10000)
            }
        }
    }

    override fun onDestroy() {
        Log.d("SafeWalkService", "SafeWalkService 종료")
        // 코루틴과 위치 추적 중단
        serviceScope.cancel()
        locationTracker.stopLocationUpdates()
        super.onDestroy()
    }
}