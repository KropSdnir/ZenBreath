package com.example.zenbreath.service

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to monitor heart rate using Health Services
 */
@Singleton
class HeartRateService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient

    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable

    /**
     * Check if heart rate sensor is available
     */
    suspend fun checkAvailability() {
        try {
            val capabilitiesFuture: ListenableFuture<androidx.health.services.client.data.MeasureCapabilities> =
                measureClient.getCapabilitiesAsync()
            val capabilities = capabilitiesFuture.await()
            _isAvailable.value = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
            Log.d(TAG, "Heart rate sensor available: ${_isAvailable.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking heart rate availability", e)
            _isAvailable.value = false
        }
    }

    /**
     * Get current heart rate
     */
    fun getCurrentHeartRate(): Int {
        return _heartRate.value
    }

    /**
     * Update heart rate (for simulation/testing)
     */
    fun updateHeartRate(rate: Int) {
        _heartRate.value = rate
    }

    companion object {
        private const val TAG = "HeartRateService"
    }
}
