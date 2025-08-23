// SafeWalkService.kt 파일 생성
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.selfbell.core.location.LocationTracker
import com.selfbell.domain.repository.SafeWalkRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint // ✅ Hilt 사용을 위해 추가


@AndroidEntryPoint // ✅ Hilt 사용
class SafeWalkService : Service() {



    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var safeWalkRepository: SafeWalkRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getLongExtra("SESSION_ID", -1L)
        if (sessionId == -1L) {
            stopSelf() // 세션 ID가 없으면 서비스 종료
            return START_NOT_STICKY
        }
        Log.d("SafeWalkService", "Service started")
        // ⚠️ TODO: 포그라운드 서비스 시작 알림 표시 로직 추가

        // ⚠️ TODO: 여기서부터 위치 추적 및 서버 전송 로직 구현
        CoroutineScope(Dispatchers.IO).launch {
            locationTracker.getLocationUpdates().collect { location ->
                // ⚠️ 여기에서 1분(60초)마다 전송 로직을 구현해야 함.
                // 10초마다 위치를 받으므로, 6번째 업데이트마다 서버 전송 가능.
                // 또는 `delay(60000)`를 사용해 주기적으로 위치를 가져와 전송
                safeWalkRepository.uploadLocationTrack(
                    sessionId = sessionId,
                    lat = location.latitude,
                    lon = location.longitude,
                    accuracy = location.accuracy.toDouble()
                )
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // ✅ CoroutineScope.cancel() 및 위치 추적 중단
        locationTracker.stopLocationUpdates()
        super.onDestroy()
    }
}