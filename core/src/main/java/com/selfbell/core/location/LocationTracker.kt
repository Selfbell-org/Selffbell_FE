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
		if (
			ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
		) {
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

		fusedLocationClient.requestLocationUpdates(
			locationRequest,
			locationCallback,
			Looper.getMainLooper()
		).addOnFailureListener { exception ->
			close(exception)
		}

		awaitClose {
			fusedLocationClient.removeLocationUpdates(locationCallback)
		}
	}

	fun stopLocationUpdates() {
		fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
	}
}


