package com.example.zenbreath.ui.screens.home

import androidx.compose.runtime.Immutable
import com.example.zenbreath.data.BreathingSession

@Immutable
data class HomeUiState(
    val timerDuration: Long = 60000L,
    val remainingTime: Long = 60000L,
    val totalReps: Int = 10,
    val currentRep: Int = 0,
    val isRunning: Boolean = false,
    val currentHeartRate: Int = 0,
    val selectedDate: Long = System.currentTimeMillis(),
    val timerColor: Long = 0xFF6200EE,
    val filteredSessions: List<BreathingSession> = emptyList(),
    val recentSessions: List<BreathingSession> = emptyList()
)
