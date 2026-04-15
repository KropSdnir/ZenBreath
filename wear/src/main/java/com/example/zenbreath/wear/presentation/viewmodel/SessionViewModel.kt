package com.example.zenbreath.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenbreath.wear.data.health.HeartRateMonitor
import com.example.zenbreath.wear.data.local.WearBreathingSession
import com.example.zenbreath.wear.data.local.WearSessionDao
import com.example.zenbreath.wear.data.sync.WearSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val heartRateMonitor: HeartRateMonitor,
    private val sessionDao: WearSessionDao
) : ViewModel() {

    private val _currentHeartRate = MutableStateFlow(0)
    val currentHeartRate: StateFlow<Int> = _currentHeartRate.asStateFlow()

    private val heartRateSamples = mutableListOf<Int>()
    private var sessionStartTime: Long = 0
    private var startHeartRate: Int = 0

    private var monitoringJob: kotlinx.coroutines.Job? = null

    init {
        // Start tracking heart rate immediately when ViewModel is created
        // This ensures HR is available even before a session starts
        startMonitoring()
    }

    fun startMonitoring() {
        // Cancel existing job if any to ensure a fresh start (especially after permission grant)
        monitoringJob?.cancel()

        monitoringJob = viewModelScope.launch {
            android.util.Log.d("SessionViewModel", "Starting heart rate monitoring flow...")
            heartRateMonitor.observeHeartRate().collect { bpm ->
                android.util.Log.d("SessionViewModel", "BPM updated in ViewModel: $bpm")
                if (bpm > 0) {
                    _currentHeartRate.value = bpm
                }
            }
        }
    }

    fun syncHeartRateToPhone(syncManager: WearSyncManager) {
        // Redundant with WearHeartRateService but good for UI-driven updates
        viewModelScope.launch {
            _currentHeartRate.collect { bpm ->
                if (bpm > 0) {
                    android.util.Log.d("SessionViewModel", "Broadcasting BPM to phone from VM: $bpm")
                    syncManager.sendHeartRate(bpm)
                }
            }
        }
    }

    fun startTracking(syncManager: WearSyncManager) {
        android.util.Log.d("SessionViewModel", "Starting session tracking...")
        heartRateSamples.clear()
        sessionStartTime = System.currentTimeMillis()
        startHeartRate = _currentHeartRate.value
        
        viewModelScope.launch {
            _currentHeartRate.collect { bpm ->
                if (bpm > 0) {
                    if (startHeartRate == 0) startHeartRate = bpm
                    heartRateSamples.add(bpm)
                    android.util.Log.d("SessionViewModel", "Pushing session BPM to phone: $bpm")
                    syncManager.sendHeartRate(bpm)
                }
            }
        }
    }

    fun stopAndSaveSession(
        repNumber: Int = 1,
        totalReps: Int = 1,
        timerDuration: Long = 60000L
    ) {
        val endTime = System.currentTimeMillis()
        val endHeartRate = if (heartRateSamples.isNotEmpty()) heartRateSamples.last() else 0

        viewModelScope.launch {
            sessionDao.insertSession(
                WearBreathingSession(
                    startTimestamp = sessionStartTime,
                    endTimestamp = endTime,
                    startHeartRate = startHeartRate,
                    endHeartRate = endHeartRate,
                    repNumber = repNumber,
                    totalReps = totalReps,
                    timerDuration = timerDuration
                )
            )
        }
    }
}
