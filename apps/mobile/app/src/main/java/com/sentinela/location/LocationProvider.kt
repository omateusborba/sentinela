package com.sentinela.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sentinela.domain.model.Coordinate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationProvider(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinate? = suspendCoroutine { cont ->
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            cont.resume(
                location?.let { Coordinate(it.latitude, it.longitude) },
            )
        }.addOnFailureListener {
            cont.resume(null)
        }
    }
}
