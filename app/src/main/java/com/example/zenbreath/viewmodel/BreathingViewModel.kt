package com.example.zenbreath.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenbreath.data.AppDatabase
import com.example.zenbreath.data.BreathingRepository
import com.example.zenbreath.data.BreathingSession
import com.example.zenbreath.service.HeartRateService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

/**
 * ViewModel for managing breathing exercise state
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BreathingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val heartRateService: HeartRateService = HeartRateService(application)
    private val repository: BreathingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BreathingRepository(database.breathingSessionDao())

        viewModelScope.launch {
            heartRateService.checkAvailability()
        }
    }

    // Timer settings
    private val _timerDuration = MutableStateFlow(60000L) // Default 60 seconds
    val timerDuration: StateFlow<Long> = _timerDuration.asStateFlow()

    private val _remainingTime = MutableStateFlow(60000L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    // Rep settings
    private val _totalReps = MutableStateFlow(10)
    val totalReps: StateFlow<Int> = _totalReps.asStateFlow()

    private val _currentRep = MutableStateFlow(0)
    val currentRep: StateFlow<Int> = _currentRep.asStateFlow()

    // Session state
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _startTimestamp = MutableStateFlow(0L)
    private val _startHeartRate = MutableStateFlow(0)

    // Selected date for filtering
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // Heart rate
    private val _currentHeartRate = MutableStateFlow(0)
    val currentHeartRate: StateFlow<Int> = _currentHeartRate.asStateFlow()

    // Session history - filtered by selected date
    val filteredSessions: StateFlow<List<BreathingSession>> = selectedDate
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

    // Recent sessions (for export - all sessions)
    val recentSessions = repository.getRecentSessions(20)

    private var timerJob: Job? = null

    /**
     * Update timer duration (in milliseconds)
     */
    fun setTimerDuration(durationMs: Long) {
        _timerDuration.value = durationMs
        if (!_isRunning.value) {
            _remainingTime.value = durationMs
        }
    }

    /**
     * Update total reps
     */
    fun setTotalReps(reps: Int) {
        _totalReps.value = reps
    }

    // Timer color
    private val _timerColor = MutableStateFlow(0xFF6200EE) // Default Purple
    val timerColor: StateFlow<Long> = _timerColor.asStateFlow()

    /**
     * Update timer color
     */
    fun setTimerColor(color: Long) {
        _timerColor.value = color
    }

    /**
     * Start breathing exercise
     */
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

    /**
     * Stop breathing exercise
     */
    fun stopExercise() {
        if (!_isRunning.value) return

        timerJob?.cancel()
        _isRunning.value = false

        val endTimestamp = System.currentTimeMillis()
        val endHeartRate = heartRateService.getCurrentHeartRate()

        // Save session to database
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
        
        // Removed: _remainingTime.value = _timerDuration.value
        // We keep it at 0 to allow the UI to show the "Finished" state.
    }

    /**
     * Reset rep counter and timer
     */
    fun resetReps() {
        _currentRep.value = 0
        _remainingTime.value = _timerDuration.value
    }

    /**
     * Set selected date for filtering sessions
     */
    fun setSelectedDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }

    /**
     * Delete a specific session from the database
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    /**
     * Start countdown timer
     */
    private fun startTimer() {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        val initialRemaining = _remainingTime.value
        var lastHeartRateUpdate = 0L

        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _isRunning.value) {
                // MANUAL TUNING: 50ms tick rate for smooth 20fps UI updates
                delay(50)
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                _remainingTime.value = (initialRemaining - elapsed).coerceAtLeast(0)

                // Heart rate doesn't need to update as frequently as the timer
                if (currentTime - lastHeartRateUpdate >= 1000) {
                    _currentHeartRate.value = heartRateService.getCurrentHeartRate()
                    lastHeartRateUpdate = currentTime
                }
            }

            // Auto-stop when timer reaches 0
            if (_remainingTime.value <= 0) {
                stopExercise()
            }
        }
    }

    /**
     * Export sessions to CSV
     */
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
