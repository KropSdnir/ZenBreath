package com.example.zenbreath.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenbreath.data.BreathingRepository
import com.example.zenbreath.data.BreathingSession
import com.example.zenbreath.service.HeartRateService
import com.example.zenbreath.ui.screens.home.HomeUiState
import com.example.zenbreath.data.sync.WearSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for managing breathing exercise state using Hilt and UDF
 */
@HiltViewModel
class BreathingViewModel @Inject constructor(
    private val repository: BreathingRepository,
    private val heartRateService: HeartRateService,
    private val wearSyncManager: WearSyncManager
) : ViewModel() {

    private val _currentHeartRate = MutableStateFlow(0)
    private val _startTimestamp = MutableStateFlow(0L)
    private val _startHeartRate = MutableStateFlow(0)
    private val _timerDuration = MutableStateFlow(60000L)
    private val _remainingTime = MutableStateFlow(60000L)
    private val _totalReps = MutableStateFlow(10)
    private val _currentRep = MutableStateFlow(0)
    private val _isRunning = MutableStateFlow(false)
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _timerColor = MutableStateFlow(0xFF6200EE)

    init {
        viewModelScope.launch {
            heartRateService.checkAvailability()
        }

        wearSyncManager.observeHeartRate()
            .onEach { bpm ->
                Log.d("BreathingViewModel", "Received BPM from watch flow: $bpm")
                if (bpm > 0) {
                    _currentHeartRate.value = bpm
                    // Ensure the exercise knows about this if it's running
                    if (_isRunning.value) {
                        if (_startHeartRate.value == 0) {
                            _startHeartRate.value = bpm
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

        wearSyncManager.startMessageFlow
            .onEach { 
                Log.d("BreathingViewModel", "Start message received from wear")
                startExercise() 
            }
            .launchIn(viewModelScope)

        wearSyncManager.stopMessageFlow
            .onEach { 
                Log.d("BreathingViewModel", "Stop message received from wear")
                stopExercise() 
            }
            .launchIn(viewModelScope)

        combine(_isRunning, _startTimestamp) { isRunning, startTimestamp ->
            wearSyncManager.updateActiveSession(isRunning, startTimestamp)
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredSessions: StateFlow<List<BreathingSession>> = _selectedDate
        .flatMapLatest { dateMillis ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis
            repository.getSessionsByDate(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recentSessions: StateFlow<List<BreathingSession>> = repository.getRecentSessions(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        _timerDuration, _remainingTime, _totalReps, _currentRep,
        _isRunning, _selectedDate, _timerColor, _currentHeartRate,
        filteredSessions, recentSessions
    ) { args ->
        HomeUiState(
            timerDuration = args[0] as Long,
            remainingTime = args[1] as Long,
            totalReps = args[2] as Int,
            currentRep = args[3] as Int,
            isRunning = args[4] as Boolean,
            selectedDate = args[5] as Long,
            timerColor = args[6] as Long,
            currentHeartRate = args[7] as Int,
            filteredSessions = args[8] as List<BreathingSession>,
            recentSessions = args[9] as List<BreathingSession>
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

    private var timerJob: Job? = null

    fun setTimerDuration(durationMs: Long) {
        _timerDuration.value = durationMs
        if (!_isRunning.value) {
            _remainingTime.value = durationMs
        }
    }

    fun setTotalReps(reps: Int) {
        _totalReps.value = reps
    }

    fun setTimerColor(color: Long) {
        _timerColor.value = color
    }

    fun setSelectedDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }

    fun startExercise() {
        if (_isRunning.value) return
        _isRunning.value = true
        _startTimestamp.value = System.currentTimeMillis()
        
        // Use synced heart rate if available (> 0), otherwise fallback to service
        val currentHR = if (_currentHeartRate.value > 0) {
            _currentHeartRate.value
        } else {
            heartRateService.getCurrentHeartRate()
        }
        
        _startHeartRate.value = currentHR
        _currentHeartRate.value = currentHR

        _currentRep.value = _currentRep.value + 1
        _remainingTime.value = _timerDuration.value
        startTimer()
    }

    fun stopExercise() {
        if (!_isRunning.value) return
        timerJob?.cancel()
        _isRunning.value = false
        val endTimestamp = System.currentTimeMillis()
        
        // Use synced HR for end HR if available
        val endHeartRate = if (_currentHeartRate.value > 0) {
            _currentHeartRate.value
        } else {
            heartRateService.getCurrentHeartRate()
        }

        viewModelScope.launch {
            val session = BreathingSession(
                startTimestamp = _startTimestamp.value,
                endTimestamp = endTimestamp,
                startHeartRate = _startHeartRate.value,
                endHeartRate = endHeartRate,
                repNumber = _currentRep.value,
                totalReps = _totalReps.value,
                timerDuration = _timerDuration.value
            )
            repository.insertSession(session)
        }
    }

    fun resetReps() {
        _currentRep.value = 0
        _remainingTime.value = _timerDuration.value
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        val initialRemaining = _remainingTime.value
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _isRunning.value) {
                delay(50)
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                _remainingTime.value = (initialRemaining - elapsed).coerceAtLeast(0)
                
                // Only update from heartRateService if we are not getting synced data 
                // AND a local sensor is actually available
                if (_currentHeartRate.value <= 0 && heartRateService.isAvailable.value) {
                     // Check every second
                     if (elapsed % 1000 < 50) {
                         val localHR = heartRateService.getCurrentHeartRate()
                         if (localHR > 0) {
                             Log.d("BreathingViewModel", "Using local HR: $localHR")
                             _currentHeartRate.value = localHR
                         }
                     }
                }
            }
            if (_remainingTime.value <= 0) stopExercise()
        }
    }

    fun exportToCSV(context: Context, sessions: List<BreathingSession>): String {
        val csv = repository.formatSessionsToCSV(sessions)
        val fileName = "breathing_sessions_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(csv)
        return file.absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
