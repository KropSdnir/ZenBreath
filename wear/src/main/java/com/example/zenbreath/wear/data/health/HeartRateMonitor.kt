package com.example.zenbreath.wear.data.health

import android.util.Log
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataType.Companion.HEART_RATE_BPM
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.data.MeasureCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateMonitor @Inject constructor(
    private val healthServicesClient: HealthServicesClient
) {
    fun observeHeartRate(): Flow<Int> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                if (availability is DataTypeAvailability) {
                    Log.d("HeartRateMonitor", "Availability changed for ${dataType.name}: $availability")
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRateSamples = data.sampleDataPoints.filter { it.dataType == HEART_RATE_BPM }
                if (heartRateSamples.isNotEmpty()) {
                    val lastPoint = heartRateSamples.last()
                    val value = lastPoint.value
                    
                    val lastBpm = when (value) {
                        is Double -> value.toInt()
                        is Float -> value.toInt()
                        is Long -> value.toInt()
                        is Int -> value
                        else -> 0
                    }
                    
                    if (lastBpm > 0) {
                        Log.d("HeartRateMonitor", "BPM measured: $lastBpm")
                        trySend(lastBpm)
                    }
                }
            }
        }

        try {
            Log.d("HeartRateMonitor", "Checking heart rate capability...")
            val capabilities = healthServicesClient.measureClient.getCapabilitiesAsync().await()
            if (!capabilities.supportedDataTypesMeasure.contains(HEART_RATE_BPM)) {
                Log.e("HeartRateMonitor", "Heart rate is not supported on this device")
                trySend(0)
                return@callbackFlow
            }

            Log.d("HeartRateMonitor", "Registering heart rate measure callback...")
            healthServicesClient.measureClient.registerMeasureCallback(
                HEART_RATE_BPM,
                callback
            )
            Log.d("HeartRateMonitor", "Callback registered successfully")
        } catch (e: Exception) {
            Log.e("HeartRateMonitor", "Failed to register callback: ${e.message}", e)
            // Don't close immediately, maybe permission is coming soon
            // But for now we emit 0 to indicate state
            trySend(0)
        }

        awaitClose {
            Log.d("HeartRateMonitor", "Unregistering heart rate measure callback")
            try {
                healthServicesClient.measureClient.unregisterMeasureCallbackAsync(
                    HEART_RATE_BPM,
                    callback
                )
            } catch (e: Exception) {
                Log.e("HeartRateMonitor", "Failed to unregister callback", e)
            }
        }
    }
}
