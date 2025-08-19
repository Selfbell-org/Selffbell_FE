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

	private fun hasLocationPermission(): Boolean {
		return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
			   ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}
}


