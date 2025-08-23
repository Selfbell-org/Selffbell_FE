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
import com.selfbell.app.MainActivity
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    private var updateCount = 0

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
        startLocationTrackingAndReporting()

        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
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
            .setSmallIcon(R.drawable.default_profile_icon2) // ✅ 알림 아이콘
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

    private fun startLocationTrackingAndReporting() {
        serviceScope.launch {
            locationTracker.getLocationUpdates()
                .distinctUntilChanged()
                .catch { e ->
                    Log.e("SafeWalkService", "위치 추적 중 오류 발생", e)
                }
                .collect { location ->
                    // 1분(60초)마다 서버에 위치를 전송하는 로직
                    // LocationTracker의 업데이트 주기가 10초라면, 6번째 업데이트마다 전송
                    if (updateCount % 6 == 0) {
                        try {
                            safeWalkRepository.uploadLocationTrack(
                                sessionId = sessionId,
                                lat = location.latitude,
                                lon = location.longitude,
                                accuracy = location.accuracy.toDouble()
                            )
                            // WebSocket으로도 위치를 보냄
                            stompManager.sendLocation(
                                sessionId = sessionId,
                                lat = location.latitude,
                                lon = location.longitude
                            )
                        } catch (e: Exception) {
                            Log.e("SafeWalkService", "위치 정보 서버 전송 실패", e)
                        }
                    }
                    updateCount++
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