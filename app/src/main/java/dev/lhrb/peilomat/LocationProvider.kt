package dev.lhrb.peilomat

import android.annotation.SuppressLint
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

interface LocationProvider {
    suspend fun getLocation() : Coordinates
}

class LocationProviderImpl(private val fusedLocationProvider: FusedLocationProviderClient) :
    LocationProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun getLocation(): Coordinates {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setDurationMillis(5000)
            .build()

        return suspendCancellableCoroutine { continuation ->
            fusedLocationProvider.getCurrentLocation(locationRequest, null)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        continuation.resume(
                            Coordinates(
                                lat = location.latitude,
                                lon = location.longitude
                            )
                        ) {}
                    } else {
                        continuation.resumeWithException(
                            task.exception ?: Exception("Unknown error")
                        )
                    }
                }
        }
    }
}