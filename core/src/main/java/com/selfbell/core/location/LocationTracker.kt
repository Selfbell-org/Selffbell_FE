package com.selfbell.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
	private val context: Context
) {
	private val fusedLocationClient: FusedLocationProviderClient by lazy {
		LocationServices.getFusedLocationProviderClient(context)
	}

	private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
		.setMinUpdateDistanceMeters(10f)
		.build()

	fun getLocationUpdates(): Flow<Location> = callbackFlow {
		// 권한 체크를 더 엄격하게 수행
		if (!hasLocationPermission()) {
			close(Exception("위치 권한이 필요합니다."))
			return@callbackFlow
		}

		val locationCallback = object : LocationCallback() {
			override fun onLocationResult(locationResult: LocationResult) {
				locationResult.lastLocation?.let { location ->
					trySend(location)
				}
			}
		}

		// 권한이 있을 때만 위치 업데이트 요청
		if (hasLocationPermission()) {
			fusedLocationClient.requestLocationUpdates(
				locationRequest,
				locationCallback,
				Looper.getMainLooper()
			).addOnFailureListener { exception ->
				close(exception)
			}
		} else {
			close(Exception("위치 권한이 필요합니다."))
			return@callbackFlow
		}

		awaitClose {
			if (hasLocationPermission()) {
				fusedLocationClient.removeLocationUpdates(locationCallback)
			}
		}
	}

	fun stopLocationUpdates() {
		if (hasLocationPermission()) {
			fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
		}
	}

	fun getLastKnownLocation(): Location? {
		return if (hasLocationPermission()) {
			try {
				// FusedLocationProviderClient의 getLastLocation() 메서드 사용
				val task = fusedLocationClient.lastLocation
				// 동기적으로 결과를 기다림 (주의: 메인 스레드에서 호출하면 안됨)
				if (task.isComplete) {
					task.result
				} else {
					null
				}
			} catch (e: Exception) {
				null
			}
		} else {
			null
		}
	}

	// ✅ 개선된 버전: 위치 권한과 GPS 상태를 확인하고 더 자세한 로깅 제공
	suspend fun getLastKnownLocationWithLogging(): Location? {
		if (!hasLocationPermission()) {
			android.util.Log.w("LocationTracker", "위치 권한이 없습니다.")
			return null
		}

		try {
			val task = fusedLocationClient.lastLocation
			// 코루틴 내에서 안전하게 결과를 기다림
			val location = task.await()
			if (location != null) {
				android.util.Log.d("LocationTracker", "마지막 위치 획득: lat=${location.latitude}, lon=${location.longitude}, accuracy=${location.accuracy}m")
				return location
			} else {
				android.util.Log.w("LocationTracker", "마지막 위치가 null입니다. GPS가 활성화되어 있는지 확인하세요.")
				return null
			}
		} catch (e: Exception) {
			android.util.Log.e("LocationTracker", "위치 획득 중 오류 발생", e)
			return null
		}
	}

	private fun hasLocationPermission(): Boolean {
		return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
			   ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}
}


