package com.example.zenbreath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model for a breathing session
 * Stores start/end timestamps and heart rate data
 */
@Entity(tableName = "breathing_sessions")
data class BreathingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,          // S: when start button was tapped
    val endTimestamp: Long,            // E: when end button was tapped
    val startHeartRate: Int,           // SHR: heart rate at start
    val endHeartRate: Int,             // EHR: heart rate at end
    val repNumber: Int,                // Which rep this was (e.g., 1/10)
    val totalReps: Int,                // Total reps planned (e.g., 10)
    val timerDuration: Long            // Duration of timer in milliseconds
)
