package com.example.zenbreath.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenbreath.data.BreathingRepository
import com.example.zenbreath.data.BreathingSession
import com.example.zenbreath.service.HeartRateService
import com.example.zenbreath.ui.screens.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    private val heartRateService: HeartRateService
) : ViewModel() {

    init {
        viewModelScope.launch {
            heartRateService.checkAvailability()
        }
    }

    private val _timerDuration = MutableStateFlow(60000L)
    private val _remainingTime = MutableStateFlow(60000L)
    private val _totalReps = MutableStateFlow(10)
    private val _currentRep = MutableStateFlow(0)
    private val _isRunning = MutableStateFlow(false)
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _timerColor = MutableStateFlow(0xFF6200EE)
    private val _currentHeartRate = MutableStateFlow(0)
    private val _startTimestamp = MutableStateFlow(0L)
    private val _startHeartRate = MutableStateFlow(0)

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
        _startHeartRate.value = heartRateService.getCurrentHeartRate()
        _currentHeartRate.value = _startHeartRate.value
        _currentRep.value = _currentRep.value + 1
        _remainingTime.value = _timerDuration.value
        startTimer()
    }

    fun stopExercise() {
        if (!_isRunning.value) return
        timerJob?.cancel()
        _isRunning.value = false
        val endTimestamp = System.currentTimeMillis()
        val endHeartRate = heartRateService.getCurrentHeartRate()
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
        var lastHeartRateUpdate = 0L
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _isRunning.value) {
                delay(50)
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                _remainingTime.value = (initialRemaining - elapsed).coerceAtLeast(0)
                if (currentTime - lastHeartRateUpdate >= 1000) {
                    _currentHeartRate.value = heartRateService.getCurrentHeartRate()
                    lastHeartRateUpdate = currentTime
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
